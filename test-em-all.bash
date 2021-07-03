#!/usr/bin/env bash
# Sample usage: HOST=localhost PORT=7000 ./test-em-all.bash

# shellcheck disable=SC2223
: ${HOST=localhost}
# shellcheck disable=SC2223
: ${PORT=8443}
# shellcheck disable=SC2223
: ${PLAYER_ID_REVS_RECS=2}
# shellcheck disable=SC2223
: ${PLAYER_ID_NOT_FOUND=13}
# shellcheck disable=SC2223
: ${PLAYER_ID_NO_RECS=114}
# shellcheck disable=SC2223
: ${PLAYER_ID_NO_REVS=214}

function assertCurl() {
  local expectedHttpCode=$1
  local curlCmd="$2 -w \"%{http_code}\""
  # shellcheck disable=SC2155
  local result=$(eval "$curlCmd")
  local httpCode="${result:(-3)}"
  RESPONSE='' && ((${#result} > 3)) && RESPONSE="${result%???}"

  if [ "$httpCode" = "$expectedHttpCode" ]; then
    if [ "$httpCode" = "200" ]; then
      echo "Test OK (HTTP Code: $httpCode)"
    else
      echo "Test OK (HTTP Code: $httpCode, $RESPONSE)"
    fi
    return 0
  else
    echo "Test FAILED, EXPECTED HTTP Code: $expectedHttpCode, GOT: $httpCode, WILL ABORT!"
    echo "- Failing command: $curlCmd"
    echo "- Response Body: $RESPONSE"
    return 1
  fi
}

function assertEqual() {
  local expected=$1
  local actual=$2

  if [ "$actual" = "$expected" ]; then
    echo "Test OK (actual value: $actual)"
    return 0
  else
    echo "Test FAILED, EXPECTED VALUE: $expected, ACTUAL VALUE: $actual, WILL ABORT"
    return 1
  fi
}

function testUrl() {
  # shellcheck disable=SC2124
  url=$@
  if $url -ks -f -o /dev/null; then
    return 0
  else
    return 1
  fi
}

function waitForService() {
  # shellcheck disable=SC2124
  url=$@
  echo -n "Wait for: $url... "
  n=0
  until testUrl "$url"; do
    n=$((n + 1))
    if [[ $n == 100 ]]; then
      echo " Give up"
      exit 1
    else
      sleep 3
      echo -n ", retry #$n "
    fi
  done
  echo "DONE, continues..."
}

function testCompositeCreated() {
  # Expect that the Player Composite for playerId $PLAYER_ID_REVS_RECS has been created with one nationality, one team and one national team
  if ! assertCurl 200 "curl $AUTH -k https://$HOST:$PORT/player-composite/$PLAYER_ID_REVS_RECS -s"; then
    echo -n "FAIL"
    return 1
  fi

  set +e
  # shellcheck disable=SC2046
  assertEqual "$PLAYER_ID_REVS_RECS" $(echo "$RESPONSE" | jq .playerId)
  if [ "$?" -eq "1" ]; then return 1; fi

  # shellcheck disable=SC2046
  # shellcheck disable=SC2086
  assertEqual 1 $(echo $RESPONSE | jq ".nationality | length")
  if [ "$?" -eq "1" ]; then return 1; fi

  # shellcheck disable=SC2046
  assertEqual 1 $(echo "$RESPONSE" | jq ".team | length")
  if [ "$?" -eq "1" ]; then return 1; fi

  # shellcheck disable=SC2046
  assertEqual 1 $(echo "$RESPONSE" | jq ".nationalTeam | length")
  if [ "$?" -eq "1" ]; then return 1; fi

  set -e
}

function waitForMessageProcessing() {
  echo "Wait for messages to be processed... "

  # Give background processing some time to complete...
  sleep 1

  n=0
  until testCompositeCreated; do
    n=$((n + 1))
    if [[ $n == 40 ]]; then
      echo " Give up"
      exit 1
    else
      sleep 3
      echo -n ", retry #$n "
    fi
  done
  echo "All messages are now processed!"
}

function recreateComposite() {
  local playerId=$1
  local composite=$2

  assertCurl 200 "curl $AUTH -X DELETE -k https://$HOST:$PORT/player-composite/${playerId} -s"
  curl -X POST -k https://$HOST:$PORT/player-composite -H "Content-Type: application/json" -H "Authorization: Bearer $ACCESS_TOKEN" --data "$composite"
}

function setupTestData() {
  body="{\"playerId\":$PLAYER_ID_NO_RECS"
  body+=',"name":"player name A","surname":"player surname A", "registrationNumber":"123456", "dateOfBirth":"2021-06-30", "nationality":
    {"nationalityId":1,"name":"name 1","abbreviation":"n"}}'
  recreateComposite "$PLAYER_ID_NO_RECS" "$body"

  body="{\"playerId\":$PLAYER_ID_NO_RECS"
  body+=',"name":"player name A","surname":"player surname A", "registrationNumber":"123456", "dateOfBirth":"2021-06-30", "team":
    {"teamId":1,"name":"name 1","founded":"2021-06-30","city":"Sombor", leagueId: "1"}}'
  recreateComposite "$PLAYER_ID_NO_REVS" "$body"

  body="{\"playerId\":$PLAYER_ID_NO_RECS"
  body+=',"name":"player name A","surname":"player surname A", "registrationNumber":"123456", "dateOfBirth":"2021-06-30", "nationalTeam":
    {"nationalTeamId":1,"name":"name 1","selector":"Darko Tesic"}}'
  recreateComposite "$PLAYER_ID_REVS_RECS" "$body"

  body="{\"playerId\":$PLAYER_ID_NO_RECS"
  body+=',"name":"player name A","surname":"player surname A", "registrationNumber":"123456", "dateOfBirth":"2021-06-30", "nationality":
    {"nationalityId":1,"name":"name 1","abbreviation":"n"}, "team": {"teamId":1,"name":"name 1","founded":"2021-06-30","city":"Sombor", leagueId: "1"},
    "nationalTeam": {"nationalTeamId":1,"name":"name 1","selector":"Darko Tesic"}}'
  recreateComposite "$PLAYER_ID_NO_RECS" "$body"
}

function testCircuitBreaker() {
  echo "Start Circuit Breaker tests!"
  EXEC="docker run --rm -it --network=my-network alpine"

  # First, use the health - endpoint to verify that the circuit breaker is closed
  assertEqual "CLOSED" "$($EXEC wget player-composite:8080/actuator/health -qO - | jq -r .components.circuitBreakers.details.player.details.state)"

  # Open the circuit breaker by running three slow calls in a row, i.e. that cause a timeout exception
  # Also, verify that we get 500 back and a timeout related error message
  for ((n = 0; n < 3; n++)); do
    assertCurl 500 "curl -k https://$HOST:$PORT/player-composite/$PLAYER_ID_REVS_RECS?delay=3 $AUTH -s"
    message=$(echo "$RESPONSE" | jq -r .message)
    assertEqual "Did not observe any item or terminal signal within 2000ms" "${message:0:57}"
  done

  # Verify that the circuit breaker now is open by running the slow call again, verify it gets 200 back, i.e. fail fast works, and a response from the fallback method.
  assertCurl 200 "curl -k https://$HOST:$PORT/player-composite/$PLAYER_ID_REVS_RECS?delay=3 $AUTH -s"
  assertEqual "Fallback player2" "$(echo "$RESPONSE" | jq -r .name)"

  # Also, verify that the circuit breaker is open by running a normal call, verify it also gets 200 back and a response from the fallback method.
  assertCurl 200 "curl -k https://$HOST:$PORT/player-composite/$PLAYER_ID_REVS_RECS $AUTH -s"
  assertEqual "Fallback player2" "$(echo "$RESPONSE" | jq -r .name)"

  # Verify that a 404 (Not Found) error is returned for a non existing playerId ($PLAYER_ID_NOT_FOUND) from the fallback method.
  assertCurl 404 "curl -k https://$HOST:$PORT/player-composite/$PLAYER_ID_NOT_FOUND $AUTH -s"
  assertEqual "Player Id: $PLAYER_ID_NOT_FOUND not found in fallback cache!" "$(echo "$RESPONSE" | jq -r .message)"

  # Wait for the circuit breaker to transition to the half open state (i.e. max 10 sec)
  echo "Will sleep for 10 sec waiting for the CB to go Half Open..."
  sleep 10

  # Verify that the circuit breaker is in half open state
  assertEqual "HALF_OPEN" "$($EXEC wget player-composite:8080/actuator/health -qO - | jq -r .components.circuitBreakers.details.player.details.state)"

  # Close the circuit breaker by running three normal calls in a row
  # Also, verify that we get 200 back and a response based on information in the player database
  for ((n = 0; n < 3; n++)); do
    assertCurl 200 "curl -k https://$HOST:$PORT/player-composite/$PLAYER_ID_REVS_RECS $AUTH -s"
    assertEqual "player name C" "$(echo "$RESPONSE" | jq -r .name)"
  done

  # Verify that the circuit breaker is in closed state again
  assertEqual "CLOSED" "$($EXEC wget player-composite:8080/actuator/health -qO - | jq -r .components.circuitBreakers.details.player.details.state)"

  # Verify that the expected state transitions happened in the circuit breaker
  assertEqual "CLOSED_TO_OPEN" "$($EXEC wget player-composite:8080/actuator/circuitbreakerevents/player/STATE_TRANSITION -qO - | jq -r .circuitBreakerEvents[-3].stateTransition)"
  assertEqual "OPEN_TO_HALF_OPEN" "$($EXEC wget player-composite:8080/actuator/circuitbreakerevents/player/STATE_TRANSITION -qO - | jq -r .circuitBreakerEvents[-2].stateTransition)"
  assertEqual "HALF_OPEN_TO_CLOSED" "$($EXEC wget player-composite:8080/actuator/circuitbreakerevents/player/STATE_TRANSITION -qO - | jq -r .circuitBreakerEvents[-1].stateTransition)"
}

set -e

# shellcheck disable=SC2046
# shellcheck disable=SC2006
echo "Start Tests:" $(date)

echo "HOST=${HOST}"
echo "PORT=${PORT}"

# shellcheck disable=SC2199
if [[ $@ == *"start"* ]]; then
  echo "Restarting the test environment..."
  echo "$ docker-compose down --remove-orphans"
  docker-compose down --remove-orphans
  echo "$ docker-compose up -d"
  docker-compose up -d
fi

waitForService curl -k https://$HOST:$PORT/actuator/health

ACCESS_TOKEN=$(curl -k https://writer:secret@$HOST:$PORT/oauth/token -d grant_type=password -d username=magnus -d password=password -s | jq .access_token -r)
AUTH="-H \"Authorization: Bearer $ACCESS_TOKEN\""

setupTestdata

waitForMessageProcessing

# Verify that a normal request works, expect one nationality, one team and one national team
assertCurl 200 "curl -k https://$HOST:$PORT/player-composite/$PLAYER_ID_REVS_RECS $AUTH -s"
# shellcheck disable=SC2046
assertEqual "$PLAYER_ID_REVS_RECS" $(echo "$RESPONSE" | jq .playerId)
# shellcheck disable=SC2046
assertEqual 1 $(echo "$RESPONSE" | jq ".nationality | length")
# shellcheck disable=SC2046
assertEqual 1 $(echo "$RESPONSE" | jq ".team | length")
# shellcheck disable=SC2046
assertEqual 1 $(echo "$RESPONSE" | jq ".nationalTeam | length")

# Verify that a 404 (Not Found) error is returned for a non existing playerId ($PLAYER_ID_NOT_FOUND)
assertCurl 404 "curl -k https://$HOST:$PORT/player-composite/$PLAYER_ID_NOT_FOUND $AUTH -s"

# Verify that nationality is not returned for playerId $PLAYER_ID_NO_RECS
assertCurl 200 "curl -k https://$HOST:$PORT/player-composite/$PLAYER_ID_NO_RECS $AUTH -s"
# shellcheck disable=SC2046
# shellcheck disable=SC2086
assertEqual "$PLAYER_ID_NO_RECS" $(echo $RESPONSE | jq .playerId)
# shellcheck disable=SC2046
assertEqual 0 $(echo "$RESPONSE" | jq ".nationality | length")
# shellcheck disable=SC2046
assertEqual 1 $(echo "$RESPONSE" | jq ".team | length")
# shellcheck disable=SC2046
assertEqual 1 $(echo "$RESPONSE" | jq ".nationalTeam | length")

# Verify that team is not returned for playerId $PLAYER_ID_NO_REVS
assertCurl 200 "curl -k https://$HOST:$PORT/player-composite/$PLAYER_ID_NO_REVS $AUTH -s"
# shellcheck disable=SC2046
assertEqual $PLAYER_ID_NO_REVS $(echo "$RESPONSE" | jq .playerId)
# shellcheck disable=SC2046
# shellcheck disable=SC2086
assertEqual 1 $(echo $RESPONSE | jq ".nationality | length")
# shellcheck disable=SC2046
# shellcheck disable=SC2086
assertEqual 0 $(echo $RESPONSE | jq ".team | length")
# shellcheck disable=SC2046
# shellcheck disable=SC2086
assertEqual 1 $(echo $RESPONSE | jq ".nationalTeam | length")

# Verify that national team is not returned for playerId $PLAYER_ID_NO_REVS
assertCurl 200 "curl -k https://$HOST:$PORT/player-composite/$PLAYER_ID_NO_REVS $AUTH -s"
# shellcheck disable=SC2046
assertEqual $PLAYER_ID_NO_REVS $(echo "$RESPONSE" | jq .playerId)
# shellcheck disable=SC2046
# shellcheck disable=SC2086
assertEqual 1 $(echo $RESPONSE | jq ".nationality | length")
# shellcheck disable=SC2046
# shellcheck disable=SC2086
assertEqual 1 $(echo $RESPONSE | jq ".team | length")
# shellcheck disable=SC2046
# shellcheck disable=SC2086
assertEqual 0 $(echo $RESPONSE | jq ".nationalTeam | length")

# Verify that a 422 (Unprocessable Entity) error is returned for a playerId that is out of range (-1)
assertCurl 422 "curl -k https://$HOST:$PORT/player-composite/-1 $AUTH -s"
assertEqual "\"Invalid playerId: -1\"" "$(echo "$RESPONSE" | jq .message)"

# Verify that a 400 (Bad Request) error error is returned for a playerId that is not a number, i.e. invalid format
assertCurl 400 "curl -k https://$HOST:$PORT/player-composite/invalidPlayerId $AUTH -s"
assertEqual "\"Type mismatch.\"" "$(echo "$RESPONSE" | jq .message)"

# Verify that a request without access token fails on 401, Unauthorized
assertCurl 401 "curl -k https://$HOST:$PORT/player-composite/$PLAYER_ID_REVS_RECS -s"

# Verify that the reader - client with only read scope can call the read API but not delete API.
READER_ACCESS_TOKEN=$(curl -k https://reader:secret@$HOST:$PORT/oauth/token -d grant_type=password -d username=magnus -d password=password -s | jq .access_token -r)
READER_AUTH="-H \"Authorization: Bearer $READER_ACCESS_TOKEN\""

assertCurl 200 "curl -k https://$HOST:$PORT/player-composite/$PLAYER_ID_REVS_RECS $READER_AUTH -s"
assertCurl 403 "curl -k https://$HOST:$PORT/player-composite/$PLAYER_ID_REVS_RECS $READER_AUTH -X DELETE -s"

testCircuitBreaker

# shellcheck disable=SC2046
# shellcheck disable=SC2006
echo "End, all tests OK:" $(date)

# shellcheck disable=SC2199
if [[ $@ == *"stop"* ]]; then
  echo "Stopping the test environment..."
  echo "$ docker-compose down --remove-orphans"
  docker-compose down --remove-orphans
fi
