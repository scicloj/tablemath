# Change Log
All notable changes to this project will be documented in this file. This change log follows the conventions of [keepachangelog.com](http://keepachangelog.com/).

## [1-alpha2] - 2024-12-15
- in one-hot encoding, by default, removing the first value rather than last, for better compatibility with R
- in `columns-with` and `design`, columns of strings and keywords that have at most 20 distinct values are one-hot-encoded by default - thanks, @behrica
- docstring updates

## [1-alpha1] - 2024-12-15
- initial commit
