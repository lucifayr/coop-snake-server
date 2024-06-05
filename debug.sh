coords_file="$1"
msg_in_latency="$2"
wrap_on_out_of_bounds="$3"

export SNAKE_DEBUG=true

if [ ! -z "$coords_file" ]; then
    printf "-> coordinate replay file => $coords_file\n"
    export SNAKE_DEBUG_COORDS_FILE="$coords_file"
else
    printf "-> coordinate replay file => none\n"
    unset SNAKE_DEBUG_COORDS_FILE
fi

if [ ! -z "$msg_in_latency" ]; then
    printf "-> artifical input message processing latency => $msg_in_latency\n"
    export SNAKE_DEBUG_MSG_IN_LATENCY="$msg_in_latency"
else
    printf "-> artifical input message processing latency => disabled\n"
    unset SNAKE_DEBUG_MSG_IN_LATENCY
fi

if [ ! -z "$wrap_on_out_of_bounds" ]; then
    printf "-> wrap around when out of bounds => true\n"
    export SNAKE_DEBUG_WRAP_ON_OUT_OF_BOUNDS="$wrap_on_out_of_bounds"
else
    printf "-> wrap around when out of bounds => false\n"
    unset SNAKE_DEBUG_WRAP_ON_OUT_OF_BOUNDS
fi

if [ -z "$coords_file" ] && [ -z "$msg_in_latency" ] && [ -z "$wrap_on_out_of_bounds" ]; then
    printf "Debug mode disabled\n"
    unset SNAKE_DEBUG
fi
