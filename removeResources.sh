#!/bin/bash

# Default values
bucketName="bucketName"
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
  echo "  -b                 bucket name"
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
while getopts ":c:n:r:b:h" opt; do
  case $opt in
    c) config_file="$OPTARG";;
    n) clusterName="$OPTARG";;
    r) region="$OPTARG";;
    b) bucketName="$OPTARG";;
    h) usage;;
    \?) echo "Invalid option: -$OPTARG" >&2; usage;;
  esac
done

# Your script logic goes here
echo " gcloud dataproc clusters delete $clusterName --region=$region"
echo " gsutil rm -r gs://$bucketName"

# Example usage:
# ./script.sh -c my_config.txt -a value1 -b value2 -d value3