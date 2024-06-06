random_seed="$1"
msg_in_latency="$2"
wrap_on_out_of_bounds="$3"
record_frames="$4"
playback_frames="$5"

export SNAKE_DEBUG=true

if [ ! -z "$random_seed" ]; then
    printf "-> random seed => $random_seed\n"
    export SNAKE_DEBUG_SEED_RANDOM="$random_seed"
else
    printf "-> random seed => none\n"
    unset SNAKE_DEBUG_SEED_RANDOM
fi

if [ ! -z "$msg_in_latency" ]; then
    printf "-> artifical input message processing latency => $msg_in_latency\n"
    export SNAKE_DEBUG_MESSAGE_INPUT_LATENCY="$msg_in_latency"
else
    printf "-> artifical input message processing latency => disabled\n"
    unset SNAKE_DEBUG_MESSAGE_INPUT_LATENCY
fi

if [ ! -z "$wrap_on_out_of_bounds" ]; then
    printf "-> wrap around when out of bounds => $wrap_on_out_of_bounds\n"
    export SNAKE_DEBUG_WRAP_ON_OUT_OF_BOUNDS="$wrap_on_out_of_bounds"
else
    printf "-> wrap around when out of bounds => false\n"
    unset SNAKE_DEBUG_WRAP_ON_OUT_OF_BOUNDS
fi

if [ ! -z "$record_frames" ]; then
    printf "-> record frames => $record_frames\n"
    export SNAKE_DEBUG_RECORD_FRAMES="$record_frames"
else
    printf "-> record frames => false\n"
    unset SNAKE_DEBUG_RECORD_FRAMES
fi

if [ ! -z "$playback_frames" ]; then
    printf "-> playback frames => $playback_frames\n"
    export SNAKE_DEBUG_PLAYBACK_FRAMES="$playback_frames"
else
    printf "-> playback frames => false\n"
    unset SNAKE_DEBUG_PLAYBACK_FRAMES
fi

if [ -z "$msg_in_latency" ] && [ -z "$wrap_on_out_of_bounds" ] && [ -z "$record_frames" ] && [ -z "$playback_frames" ]; then
    printf "Debug mode disabled\n"
    unset SNAKE_DEBUG
fi
