#!/bin/bash

print_usage()
{
    printf "Creates a container backed by a SPARQL SELECT query.\n"
    printf "\n"
    printf "Usage:  %s options TARGET_URI\n" "$0"
    printf "\n"
    printf "Options:\n"
    printf "  -f, --cert-pem-file CERT_FILE        .pem file with the WebID certificate of the agent\n"
    printf "  -p, --cert-password CERT_PASSWORD    Password of the WebID certificate\n"
    printf "  -b, --base BASE_URI                  Base URI of the application\n"
    printf "\n"
    printf "  --title TITLE                        Title of the container\n"
    printf "  --description DESCRIPTION            Description of the container (optional)\n"
    printf "  --slug STRING                        String that will be used as URI path segment (optional)\n"
    printf "\n"
    printf "  --parent PARENT_URI                  URI of the parent container\n"
    printf "  --select SELECT_URI                  URI of the SELECT query (optional)\n"
}

hash turtle 2>/dev/null || { echo >&2 "turtle not on \$PATH. Need to set \$JENA_HOME. Aborting."; exit 1; }

args=()
while [[ $# -gt 0 ]]
do
    key="$1"

    case $key in
        -b|--base)
        base="$2"
        shift # past argument
        shift # past value
        ;;
        --title)
        title="$2"
        shift # past argument
        shift # past value
        ;;
        --description)
        description="$2"
        shift # past argument
        shift # past value
        ;;
        --parent)
        parent="$2"
        shift # past argument
        shift # past value
        ;;
        --select)
        select="$2"
        shift # past argument
        shift # past value
        ;;
        --slug)
        slug="$2"
        shift # past argument
        shift # past value
        ;;
        *)    # unknown arguments
        args+=("$1") # save it in an array for later
        shift # past argument
        ;;
    esac
done
set -- "${args[@]}" # restore args parameters

if [ -z "$base" ] ; then
    print_usage
    exit 1
fi
if [ -z "$title" ] ; then
    print_usage
    exit 1
fi
if [ -z "$parent" ] ; then
    print_usage
    exit 1
fi

args+=("-c")
args+=("${base}ns/default#Container")
args+=("-t")
args+=("text/turtle")

turtle+="@prefix def:	<ns/default#> .\n"
turtle+="@prefix dct:	<http://purl.org/dc/terms/> .\n"
turtle+="@prefix dh:	<https://www.w3.org/ns/ldt/document-hierarchy/domain#> .\n"
turtle+="@prefix sioc:	<http://rdfs.org/sioc/ns#> .\n"
turtle+="_:container a def:Container .\n"
turtle+="_:container dct:title \"${title}\" .\n"
turtle+="_:container sioc:has_parent <${parent}> .\n"
if [ -n "$select" ] ; then
    turtle+="_:container dh:select <${select}> .\n"
else
    turtle+="_:container dh:select <queries/default/select-children/#this> .\n" # default value
fi
if [ -n "$description" ] ; then
    turtle+="_:container dct:description \"${description}\" .\n"
fi
if [ -n "$slug" ] ; then
    turtle+="_:container dh:slug \"${slug}\" .\n"
fi

echo -e "$turtle" | turtle --base="$base" | ./create-document.sh "${args[@]}"