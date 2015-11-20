module.exports = function(function_name,params,success,error){
  if(typeof text !== "string") {
    text = "";
  }
  if(typeof title !== "string") {
    title = "Share";
  }
  if(typeof mimetype !== "string") {
    mimetype = "text/plain";
  }
  cordova.exec(success,error,"Share","share",[function_name,params]);
  return true;
};