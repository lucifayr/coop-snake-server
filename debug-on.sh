coords_file="$1"
msg_in_latency="$2"
wrap_on_out_of_bounds="$3"

export SNAKE_DEBUG=true

if [ ! -z "$coords_file" ]; then
    export SNAKE_DEBUG_COORDS_FILE="$coords_file"
fi

if [ ! -z "$msg_in_latency" ]; then
    export SNAKE_DEBUG_MSG_IN_LATENCY="$msg_in_latency"
fi

if [ ! -z "$wrap_on_out_of_bounds" ]; then
    export SNAKE_DEBUG_WRAP_ON_OUT_OF_BOUNDS="$wrap_on_out_of_bounds"
fi
