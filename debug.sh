coords_file="$1"
msg_in_latency="$2"
wrap_on_out_of_bounds="$3"

export SNAKE_DEBUG=true

if [ ! -z "$coords_file" ]; then
    printf "-> coordinate replay file => $coords_file"
    export SNAKE_DEBUG_COORDS_FILE="$coords_file"
else
    printf "-> coordinate replay file => none"
    unset SNAKE_DEBUG_COORDS_FILE
fi

if [ ! -z "$msg_in_latency" ]; then
    printf "-> artifical input message processing latency => $msg_in_latency"
    export SNAKE_DEBUG_MSG_IN_LATENCY="$msg_in_latency"
else
    printf "-> artifical input message processing latency => disabled"
    unset SNAKE_DEBUG_MSG_IN_LATENCY
fi

if [ ! -z "$wrap_on_out_of_bounds" ]; then
    printf "-> wrap around when out of bounds => true"
    export SNAKE_DEBUG_WRAP_ON_OUT_OF_BOUNDS="$wrap_on_out_of_bounds"
else
    printf "-> wrap around when out of bounds => false"
    unset SNAKE_DEBUG_WRAP_ON_OUT_OF_BOUNDS
fi

if [ -z "$coords_file" ] && [ -z "$msg_in_latency" ] && [ -z "$wrap_on_out_of_bounds" ]; then
    printf "Debug mode disabled"
    unset SNAKE_DEBUG
fi
