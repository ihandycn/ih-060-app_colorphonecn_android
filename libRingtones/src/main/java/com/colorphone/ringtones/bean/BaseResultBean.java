package com.colorphone.ringtones.bean;

/**
 * @author sundxing
 */
public class BaseResultBean {

    /**
     * retcode : 0000
     * retdesc : 成功
     * desc : 成功
     * total : 509
     * data : [{"id":"1116479172613505024","title":"你的酒馆对我打了烊","audiourl":"http://oss.kuyinyun.com/11W2MYCO/rescloud1/dd66a06c4f3a4321aaf7a8ffa72c8dcb.aac","singer":"陈雪凝","duration":"48","listencount":"12517706","aword":"请告诉我今后怎么扛","icon":"0","mp3sz":"489899","tfns":"11111","imgurl":"http://oss.kuyinyun.com/11W2MYCO/rescloud1/8c0d4e3e35d54c3c8ffb3fb00be572e8.jpg","aacurl":"http://oss.kuyinyun.com/11W2MYCO/rescloud1/dd66a06c4f3a4321aaf7a8ffa72c8dcb.aac","aacsz":"489899","ringtype":"1","isuncheck":"1","rt":"0"},{"id":"1125176025903267840","title":"心如止水","audiourl":"http://oss.kuyinyun.com/11W2MYCO/rescloud1/e1d01a42c987431d92ba9dfb0726e0ae.aac","singer":"喻言家","duration":"48","listencount":"20269309","aword":"talking to the moon 放不下的理由","icon":"0","mp3sz":"489899","tfns":"11101","imgurl":"http://oss.kuyinyun.com/11W2MYCO/rescloud1/d885d15d53b44719b359358466fd6ff2.png","aacurl":"http://oss.kuyinyun.com/11W2MYCO/rescloud1/e1d01a42c987431d92ba9dfb0726e0ae.aac","aacsz":"489899","ringtype":"1","isuncheck":"1","rt":"0"}]
     */

    private String retcode;
    private String retdesc;
    private String desc;
    private int total;

    public String getRetcode() {
        return retcode;
    }

    public void setRetcode(String retcode) {
        this.retcode = retcode;
    }

    public String getRetdesc() {
        return retdesc;
    }

    public void setRetdesc(String retdesc) {
        this.retdesc = retdesc;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }


}
