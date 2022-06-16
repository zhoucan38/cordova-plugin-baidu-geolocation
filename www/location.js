
var cordova = require('cordova');

const baiduLocation = {
  getlocation: function (success, error) {
      exec(success, error, 'LocationPlugin', 'getlocation', []);
  }
}

module.exports = baiduLocation;
