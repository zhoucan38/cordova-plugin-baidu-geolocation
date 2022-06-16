
var cordova = require('cordova');

const baiduLocation = {
  getDistance: function (success, error) {
      exec(success, error, 'LocationPlugin', 'getlocation', []);
  }
}

module.exports = baiduLocation;
