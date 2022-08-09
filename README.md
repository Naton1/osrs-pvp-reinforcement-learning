# RSPSApp

## Vision

The vision of this project is to have a very stable and functional RuneScape Private Server source which is open source to the entire community. As an added bonus, 
all copies of this source are compatible with our webclient, which we will be opening for other servers to use in the near future. 
We believe more can be achieved when we work together as a community.

## Branching strategy:

1. Fork the repo

2. On your forked repo, execute:
```git remote add upstream https://github.com/RSPSApp/elvarg-rsps.git```

This creates a link to the main repo so you can pull bug fixes and core feature improvements. 
It also means you can open a PR to contribute to the core server base, if you fix a bug or add a feature that would be useful to other servers.

3. Then execute:
```git remote -v```

You'll see two entries for origin and two entries for upstream. Origin relates to your fork and upstream relates to the main repo (https://github.com/RSPSApp/elvarg-rsps/)

4. Now execute:
```git checkout -b "upstream" --track upstream/master```
This will create a local branch called "upstream". This will basically be your local copy of the core/main repo.

Any time you want to pull the latest changes from the core repo, just do:
```git checkout upstream```
then 
```git pull upstream master```
This will update your local copy of upstream with the latest code from the main repo.

Now you need to either create a pull request from your local upstream -> your local master, or you can just pull upstream directly into your master branch periodically.
