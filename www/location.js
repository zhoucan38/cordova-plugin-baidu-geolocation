
var exec = require('cordova/exec');

const baiduLocation = {
  getLocation: function (success, error) {
      exec(success, error, 'LocationPlugin', 'getLocation', []);
  },
  getDistance: function (inLat, inLng, outLat, outLng, success, error) {
    exec(success, error, 'LocationPlugin', 'getDistance', [inLat, inLng, outLat, outLng]);
  }
}

module.exports = baiduLocation;
