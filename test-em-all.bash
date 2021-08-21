#!/usr/bin/env bash

# shellcheck disable=SC2223
: ${HOST=localhost}
# shellcheck disable=SC2223
: ${PORT=8443}
# shellcheck disable=SC2223
: ${PLAYER_ID_REVS_RECS=2}
# shellcheck disable=SC2223
: ${PLAYER_ID_NOT_FOUND=13}
# shellcheck disable=SC2223
: ${PLAYER_ID_NO_TEAM=114}
# shellcheck disable=SC2223
: ${PLAYER_ID_NO_NATIONALITY=214}
# shellcheck disable=SC2223
: ${PLAYER_ID_NO_NATIONALTEAM=314}

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
  assertEqual 1 $(echo "$RESPONSE" | jq ".nationalteam | length")
  if [ "$?" -eq "1" ]; then return 1; fi

  set -e
}

function waitForMessageProcessing() {
  echo "Wait for messages to be processed... "
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
  body="{\"playerId\":$PLAYER_ID_NO_TEAM"
  body+=',"name":"player name A","surname":"player surname A", "registrationNumber":"123456", "dateOfBirth":"2021-06-30", "nationality":
    {"nationalityId":1,"name":"name 1","abbreviation":"n"}}'
  recreateComposite "$PLAYER_ID_NO_TEAM" "$body"

  body="{\"playerId\":$PLAYER_ID_NO_TEAM"
  body+=',"name":"player name A","surname":"player surname A", "registrationNumber":"123456", "dateOfBirth":"2021-06-30", "team":
    {"teamId":1,"name":"name 1","founded":"2021-06-30","city":"Sombor", leagueId: "1"}}'
  recreateComposite "$PLAYER_ID_NO_NATIONALITY" "$body"

  body="{\"playerId\":$PLAYER_ID_NO_TEAM"
  body+=',"name":"player name A","surname":"player surname A", "registrationNumber":"123456", "dateOfBirth":"2021-06-30", "nationalteam":
    {"nationalteamId":1,"name":"name 1","selector":"Darko Tesic"}}'
  recreateComposite "$PLAYER_ID_REVS_RECS" "$body"

  body="{\"playerId\":$PLAYER_ID_NO_TEAM"
  body+=',"name":"player name A","surname":"player surname A", "registrationNumber":"123456", "dateOfBirth":"2021-06-30", "nationality":
    {"nationalityId":1,"name":"name 1","abbreviation":"n"}, "team": {"teamId":1,"name":"name 1","founded":"2021-06-30","city":"Sombor", leagueId: "1"},
    "nationalteam": {"nationalteamId":1,"name":"name 1","selector":"Darko Tesic"}}'
  recreateComposite "$PLAYER_ID_NO_TEAM" "$body"
}

function testCircuitBreaker() {
  echo "Start Circuit Breaker tests!"
  EXEC="docker run --rm -it --network=my-network alpine"
  assertEqual "CLOSED" "$($EXEC wget player-composite:8080/actuator/health -qO - | jq -r .components.circuitBreakers.details.player.details.state)"

  for ((n = 0; n < 3; n++)); do
    assertCurl 500 "curl -k https://$HOST:$PORT/player-composite/$PLAYER_ID_REVS_RECS?delay=3 $AUTH -s"
    message=$(echo "$RESPONSE" | jq -r .message)
    assertEqual "Did not observe any item or terminal signal within 2000ms" "${message:0:57}"
  done

  assertCurl 200 "curl -k https://$HOST:$PORT/player-composite/$PLAYER_ID_REVS_RECS?delay=3 $AUTH -s"
  assertEqual "Fallback player2" "$(echo "$RESPONSE" | jq -r .name)"

  assertCurl 200 "curl -k https://$HOST:$PORT/player-composite/$PLAYER_ID_REVS_RECS $AUTH -s"
  assertEqual "Fallback player2" "$(echo "$RESPONSE" | jq -r .name)"

  assertCurl 404 "curl -k https://$HOST:$PORT/player-composite/$PLAYER_ID_NOT_FOUND $AUTH -s"
  assertEqual "Player Id: $PLAYER_ID_NOT_FOUND not found in fallback cache!" "$(echo "$RESPONSE" | jq -r .message)"

  echo "Will sleep for 10 sec waiting for the CB to go Half Open..."
  sleep 10

  assertEqual "HALF_OPEN" "$($EXEC wget player-composite:8080/actuator/health -qO - | jq -r .components.circuitBreakers.details.player.details.state)"

  for ((n = 0; n < 3; n++)); do
    assertCurl 200 "curl -k https://$HOST:$PORT/player-composite/$PLAYER_ID_REVS_RECS $AUTH -s"
    assertEqual "player name C" "$(echo "$RESPONSE" | jq -r .name)"
  done

  assertEqual "CLOSED" "$($EXEC wget player-composite:8080/actuator/health -qO - | jq -r .components.circuitBreakers.details.player.details.state)"
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

assertCurl 200 "curl -k https://$HOST:$PORT/player-composite/$PLAYER_ID_REVS_RECS $AUTH -s"
# shellcheck disable=SC2046
assertEqual "$PLAYER_ID_REVS_RECS" $(echo "$RESPONSE" | jq .playerId)
# shellcheck disable=SC2046
assertEqual 1 $(echo "$RESPONSE" | jq ".nationality | length")
# shellcheck disable=SC2046
assertEqual 1 $(echo "$RESPONSE" | jq ".team | length")
# shellcheck disable=SC2046
assertEqual 1 $(echo "$RESPONSE" | jq ".nationalteam | length")

assertCurl 404 "curl -k https://$HOST:$PORT/player-composite/$PLAYER_ID_NOT_FOUND $AUTH -s"

assertCurl 200 "curl -k https://$HOST:$PORT/player-composite/$PLAYER_ID_NO_TEAM $AUTH -s"
# shellcheck disable=SC2046
# shellcheck disable=SC2086
assertEqual "$PLAYER_ID_NO_TEAM" $(echo $RESPONSE | jq .playerId)
# shellcheck disable=SC2046
assertEqual 0 $(echo "$RESPONSE" | jq ".nationality | length")
# shellcheck disable=SC2046
assertEqual 1 $(echo "$RESPONSE" | jq ".team | length")
# shellcheck disable=SC2046
assertEqual 1 $(echo "$RESPONSE" | jq ".nationalteam | length")

assertCurl 200 "curl -k https://$HOST:$PORT/player-composite/$PLAYER_ID_NO_NATIONALITY $AUTH -s"
# shellcheck disable=SC2046
assertEqual $PLAYER_ID_NO_NATIONALITY $(echo "$RESPONSE" | jq .playerId)
# shellcheck disable=SC2046
# shellcheck disable=SC2086
assertEqual 1 $(echo $RESPONSE | jq ".nationality | length")
# shellcheck disable=SC2046
# shellcheck disable=SC2086
assertEqual 0 $(echo $RESPONSE | jq ".team | length")
# shellcheck disable=SC2046
# shellcheck disable=SC2086
assertEqual 1 $(echo $RESPONSE | jq ".nationalteam | length")

assertCurl 200 "curl -k https://$HOST:$PORT/player-composite/$PLAYER_ID_NO_NATIONALITY $AUTH -s"
# shellcheck disable=SC2046
assertEqual $PLAYER_ID_NO_NATIONALITY $(echo "$RESPONSE" | jq .playerId)
# shellcheck disable=SC2046
# shellcheck disable=SC2086
assertEqual 1 $(echo $RESPONSE | jq ".nationality | length")
# shellcheck disable=SC2046
# shellcheck disable=SC2086
assertEqual 1 $(echo $RESPONSE | jq ".team | length")
# shellcheck disable=SC2046
# shellcheck disable=SC2086
assertEqual 0 $(echo $RESPONSE | jq ".nationalteam | length")

assertCurl 422 "curl -k https://$HOST:$PORT/player-composite/-1 $AUTH -s"
assertEqual "\"Invalid playerId: -1\"" "$(echo "$RESPONSE" | jq .message)"

assertCurl 400 "curl -k https://$HOST:$PORT/player-composite/invalidPlayerId $AUTH -s"
assertEqual "\"Type mismatch.\"" "$(echo "$RESPONSE" | jq .message)"

assertCurl 401 "curl -k https://$HOST:$PORT/player-composite/$PLAYER_ID_REVS_RECS -s"

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
