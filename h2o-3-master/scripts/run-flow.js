// Generated by CoffeeScript 1.9.3
(function() {
  var error, flowContents, flowFile, fs, hostname, opts, page, parseOpts, printUsageAndExit, ref, system, timeout, timeoutArg, timeoutSecs, waitFor, webpage;

  system = require('system');

  webpage = require('webpage');

  fs = require('fs');

  phantom.onError = function(message, stacktrace) {
    var stack, t;
    if (stacktrace != null ? stacktrace.length : void 0) {
      stack = (function() {
        var j, len, results;
        results = [];
        for (j = 0, len = stacktrace.length; j < len; j++) {
          t = stacktrace[j];
          results.push(' -> ' + (t.file || t.sourceURL) + ': ' + t.line + (t["function"] ? ' (in function ' + t["function"] + ')' : ''));
        }
        return results;
      })();
      console.log(("HOST: *** ERROR *** " + message + "\n") + stack.join('\n'));
      return phantom.exit(1);
    }
  };

  printUsageAndExit = function(message) {
    console.log("*** " + message + " ***");
    console.log('Usage: phantomjs run-flow.js [--host ip:port] [--timeout seconds] --flow experiment.flow');
    console.log('    --host ip:port of a running H2O cloud      Defaults to localhost:54321');
    console.log('    --timeout max allowed runtime in seconds   Defaults to forever');
    return phantom.exit(1);
  };

  parseOpts = function(args) {
    var i, j, key, len, opts;
    console.log("Using args " + (args.join(' ')));
    if (args.length % 2 === 1) {
      printUsageAndExit('Invalid arguments');
    }
    opts = {};
    for (i = j = 0, len = args.length; j < len; i = ++j) {
      key = args[i];
      if (!(i % 2 === 0)) {
        continue;
      }
      if (key.slice(0, 2) !== '--') {
        return printUsageAndExit("Expected keyword arg. Found " + key);
      }
      opts[key] = args[i + 1];
    }
    return opts;
  };

  opts = parseOpts(system.args.slice(1));

  hostname = (ref = opts['--host']) != null ? ref : 'localhost:54321';

  console.log("Using host " + hostname);

  timeout = (timeoutArg = opts['--timeout']) ? (timeoutSecs = parseInt(timeoutArg, 10), isNaN(timeoutSecs) ? printUsageAndExit("Invalid --timeout: " + timeoutArg) : void 0, 1000 * timeoutSecs) : Infinity;

  console.log("Using timeout " + timeout + "ms");

  flowFile = opts['--flow'];

  if (!flowFile) {
    printUsageAndExit('Expected --flow argument');
  }

  console.log("Using Flow " + flowFile);

  try {
    flowContents = fs.read(flowFile);
  } catch (_error) {
    error = _error;
    console.error(error);
    phantom.exit(1);
  }

  page = webpage.create();

  page.onResourceError = function(arg) {
    var errorString, url;
    url = arg.url, errorString = arg.errorString;
    return console.log("BROWSER: *** RESOURCE ERROR *** " + url + ": " + errorString);
  };

  page.onConsoleMessage = function(message) {
    return console.log("BROWSER: " + message);
  };

  waitFor = function(test, onReady) {
    var interval, isComplete, retest, startTime;
    startTime = new Date().getTime();
    isComplete = false;
    retest = function() {
      if ((new Date().getTime() - startTime < timeout) && !isComplete) {
        console.log('HOST: Waiting for Flow to complete...');
        return isComplete = test();
      } else {
        if (isComplete) {
          onReady();
          return clearInterval(interval);
        } else {
          console.log('HOST: *** ERROR *** Timeout Exceeded');
          return phantom.exit(1);
        }
      }
    };
    return interval = setInterval(retest, 2000);
  };

  page.open("http://" + hostname + "/flow/index.html", function(status) {
    var printErrors, test;
    if (status === 'success') {
      test = function() {
        return page.evaluate(function(flowContents) {
          var context, runFlow;
          context = window.flow.context;
          if (window._phantom_started_) {
            if (window._phantom_exit_) {
              return true;
            } else {
              return false;
            }
          } else {
            runFlow = function(go) {
              var waitForFlow;
              console.log("Opening flow...");
              window._phantom_running_ = true;
              context.open('Flow', JSON.parse(flowContents));
              waitForFlow = function() {
                var errors;
                if (window._phantom_running_) {
                  return setTimeout(waitForFlow, 2000);
                } else {
                  console.log('Flow completed!');
                  errors = window._phantom_errors_;
                  return go(errors ? errors : null);
                }
              };
              console.log('Running flow...');
              context.executeAllCells(true, function(status, errors) {
                console.log("Flow finished with status: " + status);
                if (status === 'failed') {
                  window._phantom_errors_ = errors;
                }
                return window._phantom_running_ = false;
              });
              return setTimeout(waitForFlow, 2000);
            };
            console.log('Running Flow...');
            window._phantom_errors_ = null;
            window._phantom_started_ = true;
            runFlow(function(error) {
              var ref1;
              if (error) {
                console.log('*** ERROR *** Error running Flow');
                window._phantom_errors_ = (ref1 = error.message) != null ? ref1 : error;
              } else {
                console.log('Flow execution completed.');
              }
              return window._phantom_exit_ = true;
            });
            return false;
          }
        }, flowContents);
      };
      printErrors = function(errors, prefix) {
        if (prefix == null) {
          prefix = '';
        }
        if (errors) {
          if (Array.isArray(errors)) {
            return ((function() {
              var j, len, results;
              results = [];
              for (j = 0, len = errors.length; j < len; j++) {
                error = errors[j];
                results.push(printErrors(error, prefix + '  '));
              }
              return results;
            })()).join('\n');
          } else if (errors.message) {
            if (errors.cause) {
              return errors.message + '\n' + printErrors(errors.cause, prefix + '  ');
            } else {
              return errors.message;
            }
          } else {
            return errors;
          }
        } else {
          return errors;
        }
      };
      return waitFor(test, function() {
        var errors;
        errors = page.evaluate(function() {
          return window._phantom_errors_;
        });
        if (errors) {
          console.log('------------------ FAILED -------------------');
          console.log(printErrors(errors));
          console.log('---------------------------------------------');
          return phantom.exit(1);
        } else {
          return phantom.exit(0);
        }
      });
    } else {
      console.log('HOST: *** ERROR *** Unable to access network.');
      return phantom.exit(1);
    }
  });

}).call(this);
