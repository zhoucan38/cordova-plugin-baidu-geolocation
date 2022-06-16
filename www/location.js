
var exec = require('cordova/exec');

const baiduLocation = {
  getlocation: function (success, error) {
      exec(success, error, 'LocationPlugin', 'getlocation', []);
  }
}

module.exports = baiduLocation;
