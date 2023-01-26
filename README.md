# RSPSApp

## Vision

The vision of this project is to have a very stable and functional RuneScape Private Server source which is open source to the entire community. As an added bonus, 
all copies of this source are compatible with our webclient, which we will be opening for other servers to use in the near future. 
We believe more can be achieved when we work together as a community.

## Setting up your fork:

1. Fork the repo
2. Clone your fork of rhe repo using `git clone <fork url>`
3. Inside your cloned fork, execute:
```git remote add rspsapp https://github.com/RSPSApp/elvarg-rsps.git```

This creates a link to the main repo so you can pull bug fixes and core feature improvements. 
It also means you can open a PR to contribute to the core server base, if you fix a bug or add a feature that would be useful to other servers.

### Getting the latest from RSPSApp/elvarg-rsps

Any time you want to pull the latest changes from the core repo, just do:

```git checkout master```
then
```git pull rspsapp master```

### Contributing to the Core

If you want to fix something on `RSPSApp/elvarg-rsps`, you need to create a clean branch from the latest version or our master. 

This is because we want to keep `RSPSApp/elvarg-rsps` vanilla and true to the original goal of Elvarg.   

1. Create a new branch for your changes ```git checkout -b "feature-description-here" --track rspsapp/master``` 
**Change `feature-description-here` to whatever you're doing**
2. Do the work/fix
3. Commit your changes ```git add --all``` then ```git commit -m "Fixing ..."```
4. Push your changes to a remote branch ```git push```
5. Go to Github > Pull Requests > New Pull Request




