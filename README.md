# QPunishments
QPunishments is a punishment system designed and developed by Quapi for maximum staff efficiency, offering a full all-in-one system including a built-in history and proof system.
The system is designed and developed on top of the BungeeCord API which means it should work on most if not all BungeeCord forks.               
There is a second version of the plugin that is designed on top of the VelocityAPI and should always work on Velocity, however updates may release with a bit delay to account for bugs that are not present on BungeeCord.


## Features

* History and Proof system for easy tracking on players
* A full lookup system allowing for checking a player's punishment history, comments, first and last login and his name history
* Fully working per-server / global punishment system
* 99% of messages customizable (System-critical messages are non-changable)

## Commands and usages
####   Command arguments explanation:
* [-is] = Silent punishment, removal
* [duration] = The duration of the punishment, for example 7m (minutes), 7h (7 hours), 7d (7 days)
* [server:<**?**>] = A specific server to provide the punishment, for example (server is case-sensitive): 
/qban Quapi server:ExampleServer
* [reason] = The reason for the given punishment.
* [-debug] = Debug argument will show you the comment id, only works on comment module
* [module] = A specific type of punishment to lookup. Currently available: ban, mute, kick, comment
* [limit] = The amount of the entries the lookup will return, only works modules
##### History command:
The **command** argument is crucial and may be one of the following:
> editcomment, removecomment, removepunishment, getcomment
#### Commands
> * qpunishments.command.ban - /qban <**playerName**> [-s] [duration] [server:<**?**>] [reason]
> * qpunishments.command.mute - /qmute <**playerName**> [-s] [duration] [server:<**?**>] [reason]
> * qpunishments.command.kick - /qkick <**playerName**> [-s] [reason]
> * qpunishments.command.lookup - /lookup [-debug] <**playerName**> [module] [limit]
> * qpunishments.command.comment - /comment <**playerName**> <**text**>
> * qpunishments.command.reloadmessages - /reloadmessages (Reloads all plugin messages without restarting proxy)
> * qpunishments.command.history - /historyadmin <**command**> <**id**> [<**content**>] (History management for less sql tinkering incase of mistakes)
> * qpunishments.command.staffchat - /staffchat <**text**> (Private chat for staff)
> * qpunishments.command.find - /find <**playerName**> (Locates the server a player is connected to)

## Disclaimer
This plugin uses BStats for data collection, if you do not want to participate you can disable BStats in the BStats folder at /plugins/
