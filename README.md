# CSCI 3601 Lab #3 - Angular and Spark Lab <!-- omit in toc -->

[![Server Build Status](../../workflows/Server%20Java/badge.svg?branch=master)](../../actions?query=workflow%3A"Server+Java")
[![Client Build Status](../../workflows/Client%20Angular/badge.svg?branch=master)](../../actions?query=workflow%3A"Client+Angular")
[![End to End Build Status](../../workflows/End-to-End/badge.svg?branch=master)](../../actions?query=workflow%3AEnd-to-End)

- [Setup](#setup)
  - [Open the project in VS Code](#open-the-project-in-vs-code)
  - [Installing the client dependencies](#installing-the-client-dependencies)
  - [Enable ESLint in VS Code](#enable-eslint-in-vs-code)
- [Running your project](#running-your-project)
- [Testing and Continuous Integration](#testing-and-continuous-integration)
  - [Testing the client](#testing-the-client)
  - [Testing the server](#testing-the-server)
  - [End to end testing with Cypress](#end-to-end-testing-with-cypress)
  - [GitHub Actions](#github-actions)
- [Resources](#resources)
  - [Angular](#angular)
  - [Cypress (end-to-end testing)](#cypress-end-to-end-testing)

During this lab, you will use a todo API like you created in the previous lab
by building a basic client-side application using Angular. This will enable you
to better handle user input and display data returned from the server. As always, 
you'll be expected to make good use of the version control (e.g., branching for features and merging changes to the master branch as appropriate) and project management 
tools available to you: write good commit messages, test things, document issues, etc.

Your specific tasks for this lab can be found in the [LABTASKS.md](LABTASKS.md)
file in this repository.

>:warning: One thing to keep in mind is that the Angular developers provide two
major updates to Angular each year. This lab is built using Angular 11. Pay attention to
the version of Angular being used in examples and on-line documentation that you find. Most
of the time, it won't matter very much, but there are times when something you find 
doesn't match what we're doing. If things seem odd, look at the versions for the
example or documentation you're looking at just in case there's a mismatch that matters.

## Setup

As in the previous lab, you'll be using VS Code and GitKraken. Once you've all joined your
group using GitHub classroom, you can clone your repository using the command line or GitKraken:

- From the file menu, choose **Clone Repo**
- Choose GitHub.com in the middle column (as the source location of your repo)
- Browse to the location you'd like to put the local copy of this project repo
- Select the correct repo from the list of repositories
- Select **Clone the repo!**

### Open the project in VS Code

Launch Visual Studio Code, and then choose `File -> Open Folder…`. Navigate to your clone
of the repo and choose `Open`.

You may see a dialog that looks like this if you don't already have the recommended extensions:

![Dialog suggesting installation of recommended extensions](https://user-images.githubusercontent.com/1300395/72710961-bf767500-3b2d-11ea-8ea4-fbbd39c78da5.png)

Don't worry if you don't get the dialog, it is probably because you already have them all installed.

Like in previous labs, click "Install All" to automatically install them.


### Installing the client dependencies

Before you start working you will need to install the dependencies for the client.

1. Move into the `client` directory (`cd client`)
2. Run `npm install`

### Enable ESLint in VS Code

Since this is the first time we will be using ESLint there is an additional step to make sure the VS Code extension is working in the project. 

1. Hit `CTRL + SHIFT + P` (`⌘ + ⇧ + P` on Macs) to open the Command Palette. You can also find this by going to the "View" menu and clicking "Command Palette..."
2. Start typing and select "ESlint: Manage Library Execution". That should open a dialog seen below.
3. Click "Allow Everywhere"

![image](https://user-images.githubusercontent.com/1300395/107996971-528b1400-6fa7-11eb-89bc-afc71747f820.png)

## Running your project

- The **run** Gradle task (`./gradlew run` in the `server` directory) will still run your Javalin server, which is available at [`localhost:4567`](http://localhost:4567).
- The **build** task will still _build_ the server, but not run it.

The major difference here is that the _client_ side of your project is,
effectively, an entirely separate project from your Javalin server. We've included a full API
for the todos, which you implemented in lab 2, so no need to copy your old project over.

Once you have successfully run `npm install`, in order to serve up the _client side_ of your project, you will run 
`ng serve` (from the `client` directory as well). This will trigger the various tools in the
client side portion of the project to build and host your client side
application on their own little web-server, available by default at [`localhost:4200`](http://localhost:4200). If your server is running, you will be able to see data for users if you navigate to the right place in the project.

## Testing and Continuous Integration

There are now more testing options! You can test the client, or the server or both.

### Testing the client

From the `client` directory:
* `ng test` runs the client tests.
* `ng test --code-coverage` runs the client tests and generates a coverage file you can find in your client directory `client/coverage/client/index.html`.
Right click on `index.html` and select `Copy path` and paste it into your browser of choice. You can also drag and drop `index.html` onto the tab area of your browser and it will open it.

### Testing the server

From the `server` directory:
* `./gradlew test` runs the server tests once.
  * It generates a report you can find in `server/build/reports/tests/test/index.html`.
* `./gradlew test jacocoTestReport` runs the server tests once and creates a coverage report
  * It generates a coverage report you can find in `server/build/jacocoHtml/index.html` in addition to the regular report generated by the `test` task.

### End to end testing with Cypress

End to end (E2E) testing involves the whole software stack rather than one part of it. Our E2E tests look at the behavior of both the client and server and how they interact by simulating what a real user would do with it.

We use [Cypress](https://www.cypress.io/) for our end-to-end tests. There are a few ways to run the E2E tests. They area all started from the `client` directory and require the server be running at the same time (`./gradlew run` in the `server` directory).

* `ng e2e` both builds and serves the client and runs through all the Cypress end-to-end tests once.
* `ng e2e --watch` builds and serves the client but just opens Cypress for you to be able to run the tests you want without closing automatically.
  * This is the same as running `ng serve` and `npm run cy:open` (or `npx cypress open`) at the same time. If you are already running `ng serve` it will be easier to do this rather than closing it and running `ng e2e`.

The main page of Cypress looks like this: 

![image](https://user-images.githubusercontent.com/1300395/107994604-9af40300-6fa2-11eb-9caf-35d804281929.png)

You can click on any of the integration test files to run their tests or run them all. When you run a set of tests you will 

![image](https://user-images.githubusercontent.com/1300395/107994642-acd5a600-6fa2-11eb-8b88-1f2879e82848.png)

There are a lot of neat things you can do here like inspect each test and find which selectors to use in the tests you are writing. We encourage you to look through some of the Cypress documentation below.

### GitHub Actions

There are GitHub Actions set up in your repo for each of the three checks: JUnit tests for the server, Karma tests for the client, and Cypress tests for end-to-end testing. There are badges above that show the status of these checks on the master branch.

## Resources

### Angular

- [Testing (Karma)](https://angular.io/guide/testing)
- [Routing](https://angular.io/guide/router)
- [Forms](https://angular.io/guide/forms-overview)
- [Angular Material](https://material.angular.io/)
- [What are environments in Angular](https://angular.io/guide/build#configuring-application-environments)
- [Angular CLI](https://angular.io/cli)

### Cypress (end-to-end testing)

- [Cypress Docs](https://docs.cypress.io/)
- [Best Practices](https://docs.cypress.io/guides/references/best-practices.html)
- [Introduction to Cypress](https://docs.cypress.io/guides/core-concepts/introduction-to-cypress.html#Cypress-Can-Be-Simple-Sometimes)
- [Interacting with Elements](https://docs.cypress.io/guides/core-concepts/interacting-with-elements.html#Actionability)


