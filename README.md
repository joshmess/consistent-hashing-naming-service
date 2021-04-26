# consistent-hashing-naming-service
CSCI 4780 Programming Project 4

The Bootstrap Server Listens on port 4780.

### This project was done in its entirety by Josh, Alex, and Robert. We hereby state that we have not received unauthorized help of any form. 

#### Compile BootstrapDriver (From '~/Bootstrap/src/'):
```
$ javac BootstrapDriver.java
```
#### Compile NameserverDriver (From '~/Nameserver/src/'):
```
$ javac NameserverDriver.java
```

#### Execute BootstrapDriver (From '~/Bootstrap/src/'):
```
$ java BootstrapDriver [BOOTSTRAP_NS_CONFIG_FILE]
```
#### Execute NameserverDriver (From '~/Nameserver/src/'):
```
$ java NameserverDriver [NS_CONFIG_FILE]
```


## Contributions
In order to push updates to the master branch, a pull request and an approving review from another contributor are required. This avoids conflicts and helps with overall repository organization.

To do this, follow these instructions:
### Create a new branch
```bash
git checkout -b new-branch-name
```
### Stage changes for commit
```bash
git add --all
```
### Commit changes to the branch
```bash
git commit -m "Your commit message"
```
### Push commits to the branch
```bash
git push -u origin new-branch-name 
```
### Open a pull request
  1.  Go to the [repository](https://github.com/joshmess/AADM-Freedom-Fund) on the GitHub website
  2.  Click on the ***Pull requests*** tab
  3.  Click the ***New pull request*** button
  4.  Set `base` to `master`, and `compare` to `new-branch-name`
  5.  Click the ***Create pull request*** button
  6.  Leave a comment about what you did, then click ***Create pull request***
  
Now wait for another contributor to review and merge it.

### Review a pull request
  1.  Go to the [repository](https://github.com/joshmess/AADM-Freedom-Fund) on the GitHub website
  2.  Click on the ***Pull requests*** tab
  3.  Click on an open pull request
  4.  In the first box with a red X, click ***Add your review***
  5.  Click on the ***Review changes*** button
  6.  If everything looks good, click ***Approve request*** then ***Submit review***
  7.  Click the ***Merge pull request*** button
  8.  Click the ***Confirm merge*** button
  9.  Click the ***Delete branch*** button
