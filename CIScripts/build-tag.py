#!/usr/bin/env python3

##########################
# Get the current branch #
############################################################
# Description
# This function gets the current branch name
# by running the command `git rev-parse --abbrev-ref HEAD`
# and returns the output as a string.
# If the command fails, it will raise an exception.
############################################################
# Input
# - image-name: The name of the Docker image
# - registry: The Docker registry URL
############################################################
# Output
# - version: The current branch name
############################################################

import subprocess
import os
import argparse

def parse_args():
    """Parses command-line arguments."""
    parser = argparse.ArgumentParser(description="Build and tag Docker image.")
    parser.add_argument('-i', '--image-name', required=True, help='Name of the Docker image')
    parser.add_argument('-r', '--registry', required=True, help='Docker registry URL')
    return parser.parse_args()

args = parse_args()

def docker_build(uri, tag):
    """Builds a Docker image."""
    result = subprocess.run(['docker', 'build', '-t', f"{uri}:{tag}", '.'], stdout=subprocess.PIPE)
    return result.stdout.decode('utf-8').strip()

def get_git_branch():
    """Gets the current Git branch."""
    result = subprocess.run(['git', 'rev-parse', '--abbrev-ref', 'HEAD'], stdout=subprocess.PIPE)
    return result.stdout.decode('utf-8').strip()

def get_git_hash():
    """Gets the current Git hash."""
    result = subprocess.run(['git', 'rev-parse', '--short', 'HEAD'], stdout=subprocess.PIPE)
    return result.stdout.decode('utf-8').strip()

def create_docker_tag(branch, git_hash):
    """Creates a Docker tag."""
    return f"{branch}-{git_hash}"

if __name__ == "__main__":
    branch = get_git_branch()
    git_hash = get_git_hash()
    docker_tag = create_docker_tag(branch, git_hash)
    print(f"Docker tag: {docker_tag}")
    image_uri = f"{args.registry}/{args.image_name}"
    docker_build(uri=image_uri, tag=docker_tag)
    print(f"Built Docker image: {image_uri}:{docker_tag}")
    full_image_uri = f"{image_uri}:{docker_tag}"
    
    with open(os.environ['GITHUB_OUTPUT'], 'a') as fh:
        print(f"fullDockerPath={full_image_uri}", file=fh)
        print(f"version={docker_tag}", file=fh)