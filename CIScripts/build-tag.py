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
# Output
# - version: The current branch name
############################################################

import subprocess

def get_git_branch():
    result = subprocess.run(['git', 'rev-parse', '--abbrev-ref', 'HEAD'], stdout=subprocess.PIPE)
    return result.stdout.decode('utf-8').strip()

def get_git_hash():
    result = subprocess.run(['git', 'rev-parse', '--short', 'HEAD'], stdout=subprocess.PIPE)
    return result.stdout.decode('utf-8').strip()

def create_docker_tag(branch, git_hash):
    return f"{branch}-{git_hash}"

if __name__ == "__main__":
    branch = get_git_branch()
    git_hash = get_git_hash()
    docker_tag = create_docker_tag(branch, git_hash)
    print(f"Docker tag: {docker_tag}")
    
    print(f"::set-output name=version::{docker_tag}")