function share() {
}

share.prototype.test = function(text,title,mimetype,success,error) {
  cordova.exec(success,error,"Share","share",[text,title,mimetype]);
  return true;
};


/*
module.exports = function(text,title,mimetype,success,error){
  if(typeof text !== "string") {
    text = "";
  }
  if(typeof title !== "string") {
    title = "Share";
  }
  if(typeof mimetype !== "string") {
    mimetype = "text/plain";
  }
  cordova.exec(success,error,"Share","share",[text,title,mimetype]);
  return true;
};
*/
/*
SocialSharing.prototype.available = function (callback) {
  cordova.exec(function (avail) {
    callback(avail ? true : false);
  }, null, "SocialSharing", "available", []);
};
*/