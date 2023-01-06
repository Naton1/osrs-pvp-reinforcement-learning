# RSPSApp

## Vision

The vision of this project is to have a very stable and functional RuneScape Private Server source which is open source to the entire community. As an added bonus, 
all copies of this source are compatible with our webclient, which we will be opening for other servers to use in the near future. 
We believe more can be achieved when we work together as a community.

## Setting up your fork:

1. Fork the repo
2. Clone your fork of rhe repo using `git clone <fork url>`
3. Inside your cloned fork, execute:
```git remote add upstream https://github.com/RSPSApp/elvarg-rsps.git```

This creates a link to the main repo so you can pull bug fixes and core feature improvements. 
It also means you can open a PR to contribute to the core server base, if you fix a bug or add a feature that would be useful to other servers.

3. Then execute:
```git remote -v```

You'll see two entries for origin and two entries for upstream. Origin relates to your fork and upstream relates to the main repo (https://github.com/RSPSApp/elvarg-rsps/)

4. Now execute:
```git checkout -b "upstream" --track upstream/master```
This will create a local branch called "upstream". This will basically be your local copy of the core/main repo.

### Getting the latest from RSPSApp/elvarg-rsps

Any time you want to pull the latest changes from the core repo, just do:

```git checkout master```
then
```git pull upstream master```

You can also update your usptream branch by doing:

```git checkout upstream```
then
```git pull```

### Contributing to the Core

If you want to fix something on `RSPSApp/elvarg-rsps`, you need to create a clean branch from the latest version or our master. 

This is because we want to keep `RSPSApp/elvarg-rsps` vanilla and true to the original goal of Elvarg.   

1. Switch to your copy of `RSPSApp/elvarg-rsps` by running ```git checkout upstream```
2. Update your copy of upstream by running ```git pull upstream master```
3. ```git checkout -b "feature-name-or-description"```
4. Do the work/fix
5. Commit your changes ```git add --all``` then ```git commit -m "Fixing ..."```
6. Push your changes to a remote branch ```git push```
7. Go to Github > Pull Requests > New Pull Request




