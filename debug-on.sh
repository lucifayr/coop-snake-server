coords_file="$1"

if [ -z "$coords_file" ]; then
    printf "WARNING: no debug coordiante file set. If you want to set one valid options are:\n"
    ls ./src/main/resources/debug/
fi

export SNAKE_DEBUG=true
export SNAKE_DEBUG_COORDS_FILE="$coords_file"
