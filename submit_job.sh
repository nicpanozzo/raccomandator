#!/bin/bash

# Default values
jarFile="target/scala-2.12/raccomandator-assembly-0.1.1-SNAPSHOT.jar"
clusterName="racclu"
region="us-central1"
config_file="config.txt"

# Function to display script usage
usage() {
  echo "Usage: $0 [-c <config_file>] [-a <value1>] [-b <value2>] [-d <value3>] [-h]"
  echo "Options:"
  echo "  -c <config_file>   Specify the configuration file"
  echo "  -n <clusterName>   Specify the cluster name"
  echo "  -r <region>        Specify the region"
  echo "  -j <jarFile>       Specify the jar file"
  echo "  -h                 Display this help message"
  exit 1
}


# Load configuration from file if provided
if [ -n "$config_file" ]; then
  if [ -f "$config_file" ]; then
    source "$config_file"
  else
    echo "Error: Configuration file '$config_file' not found."
    exit 1
  fi
fi


# Parse command line options
while getopts ":c:n:r:j:h" opt; do
  case $opt in
    c) config_file="$OPTARG";;
    n) clusterName="$OPTARG";;
    r) region="$OPTARG";;
    j) jarFile="$OPTARG";;
    h) usage;;
    \?) echo "Invalid option: -$OPTARG" >&2; usage;;
  esac
done

# Your script logic goes here
#or take the jar file from the bucket
# echo " gcloud dataproc jobs submit spark --cluster=$clusterName --region=$region --jar=gs://$bucketName/$jarFile"
echo " gcloud dataproc jobs submit spark --cluster=$clusterName --region=$region --jar=$jarFile"

# Example usage:
# ./script.sh -c my_config.txt -a value1 -b value2 -d value3