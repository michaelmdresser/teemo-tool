# teemo-tool

Teemo Tool is a project intended to help bettors on the [Salty Teemo](https://www.twitch.tv/saltyteemo) stream make more informed betting decisions. This project consists of several components:

### Project Repositories

- Twitch IRC Bot: This repository! Connects to the Salty Teemo stream chat to record bets in a SQLite database. More details below.
- [API Server](https://github.com/michaelmdresser/teemo-tool-api): Reads from the IRC bot's database. Exposes a simplistic REST API that queries the database for bets on the current match.
- [Website](https://github.com/michaelmdresser/teemo-tool-site): Visualizes data exposed by the API server. Accessible live [here](https://teemotool.com).
- [Riot API Crawler](https://github.com/michaelmdresser/teemo-tool-data): Gathers data for attempts at predicting outcome of Salty Teemo matches.
- [Match Prediction Models](https://github.com/michaelmdresser/teemo-tool-predict): Uses the data from the Riot API Crawler to build models aimed at predicting the outcome of Salty Teemo matches.


## This Repository: IRC Bot

Uses the wonderful [irclj](https://github.com/Raynes/irclj) library to connect to Twitch's [IRC API](https://dev.twitch.tv/docs/irc). Reads messages from the Salty Teemo chatroom, filters to messages from the stream bot "xxsaltbotxx", and parses them for bet confirmed messages. Confirmed bets are inserted into the SQLite database with the bettor name, bet amount, and team. The timestamp is automatically appended by the default value of the column.

## Usage

`lein run` until I package it better.

## Future Changes

- Add config files/args for database location
