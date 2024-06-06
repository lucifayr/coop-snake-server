# coop-snake server 🐍

## Build and Run

```sh
./mvnw package && java -jar target/coopsnakeserver-0.1.0.jar
```

> with tests 

---
```sh
mvn package -DskipTests && java -jar target/coopsnakeserver-0.1.0.jar
```

> without running tests 

## Development utilities 

The script `debug.sh` can be used to easily set environment variables that can
be useful for debugging such as wrapping around when going out of bounds instead
of triggering a game over, or seeding random numbers.

### Session recording and replay

```sh
# enable recording and replay 
source ./debug.sh "" "" "" true true
                            ^     ^
                            |    enable playback of recorded frames
                            |
                          enable recording of player input to be played back later
# run application
mvn package ...
```

`ls src/main/resources/debug/recordings/` shows a list of sessions that have
been recorded. Each session contains 1 file per player. 


If replay is enabled and a matching session recording exists, that recording is
used instead of allowing player input. If no recording exists, normal gameplay
is permitted.

You can send a message to a sessions to force it to use a specific recording. To
do this send a binary message starting with `01111111` (`127`) followed by `4`
bytes that represent the session key and `1`  byte that represents the
player. 

You can do this using `websocat` (similar to `curl` but for websockets), like
follows.

```sh
#                           hex encoded player byte (in this case, 1)
#                             |
printf "\x7f\x00\x00\xea\x3a\x01" |  websocat -b -n -k 'ws://0.0.0.0:8080/{session-endpoint}'
#        ^   ^^^^^^^^^^^^^^^        
#        |   hex encoded session bytes (in this case, 059962)
#        |
#   hex encoded 127 byte
```
