# cordova-plugin-baidu-location


cordova plugin add https://github.com/zhoucan38/cordova-plugin-baidu-location.git --variable API_KEY=百度分配的AK --save

window.baiduLocation.getLocation((location) => {
 console.log(location);
}, (e) => {
  console.log(e);
})
