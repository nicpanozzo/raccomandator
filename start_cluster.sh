#!/bin/bash

# Default values
bucketName="bucketName"
data="datasets/u.data"
clusterName="racclu"
clusterYaml="cluster.yaml"
region="us-central1"
config_file="config.txt"

# Function to display script usage
usage() {
  echo "Usage: $0 [-c <config_file>] [-a <value1>] [-b <value2>] [-d <value3>] [-h]"
  echo "Options:"
  echo "  -c <config_file>   Specify the configuration file"
  echo "  -h                 Display this help message"
  echo "  -b                 bucket name"
  echo "  -d                 data file"
  echo "  -n                 cluster name"
  echo "  -y                 cluster yaml file"
  echo "  -r                 region"
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
while getopts ":c:b:d:n:y:r:h" opt; do
  case $opt in
    c) config_file="$OPTARG";;
    b) bucketName="$OPTARG";;
    d) data="$OPTARG";;
    n) clusterName="$OPTARG";;
    y) clusterYaml="$OPTARG";;
    r) region="$OPTARG";;
    h) usage;;
    \?) echo "Invalid option: -$OPTARG" >&2; usage;;
  esac
done

# Your script logic goes here
gcloud storage buckets create gs://$bucketName --location=$region
gsutil cp $jarFile gs://$bucketName 
gsutil cp $data gs://$bucketName
gcloud dataproc clusters import $clusterName --source $clusterYaml --region=$region


# Example usage:
# ./script.sh -c my_config.txt -a value1 -b value2 -d value3