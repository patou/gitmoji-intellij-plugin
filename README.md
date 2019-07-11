# gitmoji-intellij-plugin

Intellij plugin for add a button on the commit dialog to add gitmoji.

![Button to add gitmoji](screenshot/gitmoji-button.png)
![List to choose gitmoji](screenshot/gitmoji-list.png)

## Installation

https://plugins.jetbrains.com/plugin/12383-gitmoji/

In IntelliJ, go to preference, then Plugins, and search Gitmoji by Patrice de Saint Steban.
After install, and restart, you will have a button on the commit dialog.


## Publish plugin

First time, copy gradle.properties.sample to gradle.properties

- Go to https://hub.jetbrains.com/users/me?tab=authentification
- Create a new token with Plugin Repository
- Copy the generated token in the gradle.properties

Execute the publishPlugin gradle task
