package com.colorphone.smartlocker.baidu;

import android.support.annotation.Keep;

import java.util.List;

@Keep
public class BaiduNewsRequest {

    @Keep
    public static class DataBean {
        /**
         * device : {"deviceType":1,"osType":8,"osVersion":"4.1.2","vendor":"samsung","model":"SM-G3556D","screenSize":{"width":1440,"height":2560},"udid":{"imei":"3524190602316321432432","imeiMd5":"24d05rerwetref85d718a511639edf5dceede2ad612","androidId":"ffsdagehtrwhyju"}}
         * network : {"ipv4":"220.181.16.0","connectionType":4,"operatorType":3}
         * contentParams : {"pageSize":5,"pageIndex":1,"contentType":0,"catIds":[],"adCount":3}
         */

        private DeviceBean device;
        private NetworkBean network;
        private ContentParamsBean contentParams;

        public DeviceBean getDevice() {
            return device;
        }

        public void setDevice(DeviceBean device) {
            this.device = device;
        }

        public NetworkBean getNetwork() {
            return network;
        }

        public void setNetwork(NetworkBean network) {
            this.network = network;
        }

        public ContentParamsBean getContentParams() {
            return contentParams;
        }

        public void setContentParams(ContentParamsBean contentParams) {
            this.contentParams = contentParams;
        }

        @Keep
        public static class DeviceBean {
            /**
             * deviceType : 1
             * osType : 8
             * osVersion : 4.1.2
             * vendor : samsung
             * model : SM-G3556D
             * screenSize : {"width":1440,"height":2560}
             * udid : {"imei":"3524190602316321432432","imeiMd5":"24d05rerwetref85d718a511639edf5dceede2ad612","androidId":"ffsdagehtrwhyju"}
             */

            private int deviceType;
            private int osType;
            private String osVersion;
            private String vendor;
            private String model;
            private ScreenSizeBean screenSize;
            private UdidBean udid;

            public int getDeviceType() {
                return deviceType;
            }

            public void setDeviceType(int deviceType) {
                this.deviceType = deviceType;
            }

            public int getOsType() {
                return osType;
            }

            public void setOsType(int osType) {
                this.osType = osType;
            }

            public String getOsVersion() {
                return osVersion;
            }

            public void setOsVersion(String osVersion) {
                this.osVersion = osVersion;
            }

            public String getVendor() {
                return vendor;
            }

            public void setVendor(String vendor) {
                this.vendor = vendor;
            }

            public String getModel() {
                return model;
            }

            public void setModel(String model) {
                this.model = model;
            }

            public ScreenSizeBean getScreenSize() {
                return screenSize;
            }

            public void setScreenSize(ScreenSizeBean screenSize) {
                this.screenSize = screenSize;
            }

            public UdidBean getUdid() {
                return udid;
            }

            public void setUdid(UdidBean udid) {
                this.udid = udid;
            }

            @Keep
            public static class ScreenSizeBean {
                /**
                 * width : 1440
                 * height : 2560
                 */

                private int width;
                private int height;

                public int getWidth() {
                    return width;
                }

                public void setWidth(int width) {
                    this.width = width;
                }

                public int getHeight() {
                    return height;
                }

                public void setHeight(int height) {
                    this.height = height;
                }
            }

            @Keep
            public static class UdidBean {
                /**
                 * imei : 3524190602316321432432
                 * imeiMd5 : 24d05rerwetref85d718a511639edf5dceede2ad612
                 * androidId : ffsdagehtrwhyju
                 */

                private String imei;
                private String imeiMd5;
                private String androidId;

                public String getImei() {
                    return imei;
                }

                public void setImei(String imei) {
                    this.imei = imei;
                }

                public String getImeiMd5() {
                    return imeiMd5;
                }

                public void setImeiMd5(String imeiMd5) {
                    this.imeiMd5 = imeiMd5;
                }

                public String getAndroidId() {
                    return androidId;
                }

                public void setAndroidId(String androidId) {
                    this.androidId = androidId;
                }
            }
        }

        @Keep
        public static class NetworkBean {
            /**
             * ipv4 : 220.181.16.0
             * connectionType : 4
             * operatorType : 3
             */

            private String ipv4;
            private int connectionType;
            private int operatorType;

            public String getIpv4() {
                return ipv4;
            }

            public void setIpv4(String ipv4) {
                this.ipv4 = ipv4;
            }

            public int getConnectionType() {
                return connectionType;
            }

            public void setConnectionType(int connectionType) {
                this.connectionType = connectionType;
            }

            public int getOperatorType() {
                return operatorType;
            }

            public void setOperatorType(int operatorType) {
                this.operatorType = operatorType;
            }
        }

        @Keep
        public static class ContentParamsBean {
            /**
             * catIds : [1016]
             * contentTypeInfos : [{"dataType":0,"catIds":[1001,1002,1003,1005,1006,1007,1008,1009,1010,1011,1012,1013,1014,1015,1016,1017,1018]},{"dataType":2,"catIds":[1001,1002,1003,1005,1006,1007,1008,1009,1010,1011,1012,1013,1014,1015,1016,1017,1018]}]
             * contentType : 0
             * listScene : 0
             * pageIndex : 2
             * pageSize : 10
             */

            private int contentType;
            private int listScene;
            private int pageIndex;
            private int pageSize;
            private List<Integer> catIds;

            public int getContentType() {
                return contentType;
            }

            public void setContentType(int contentType) {
                this.contentType = contentType;
            }

            public int getListScene() {
                return listScene;
            }

            public void setListScene(int listScene) {
                this.listScene = listScene;
            }

            public int getPageIndex() {
                return pageIndex;
            }

            public void setPageIndex(int pageIndex) {
                this.pageIndex = pageIndex;
            }

            public int getPageSize() {
                return pageSize;
            }

            public void setPageSize(int pageSize) {
                this.pageSize = pageSize;
            }

            public List<Integer> getCatIds() {
                return catIds;
            }

            public void setCatIds(List<Integer> catIds) {
                this.catIds = catIds;
            }

        }
    }

    /**
     * signature : a5c9ba312de50fea989e9865fc909ea5
     * timestamp : 1510036402528
     * appsid : xxxxx
     * token : fdasfds
     * data : {"device":{"deviceType":1,"osType":8,"osVersion":"4.1.2","vendor":"samsung","model":"SM-G3556D","screenSize":{"width":1440,"height":2560},"udid":{"imei":"3524190602316321432432","imeiMd5":"24d05rerwetref85d718a511639edf5dceede2ad612","androidId":"ffsdagehtrwhyju"}},"network":{"ipv4":"220.181.16.0","connectionType":4,"operatorType":3},"contentParams":{"pageSize":5,"pageIndex":1,"contentType":0,"catIds":[],"adCount":3}}
     */

    private String signature;
    private long timestamp;
    private String appsid;
    private String token;
    private DataBean data;

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getAppsid() {
        return appsid;
    }

    public void setAppsid(String appsid) {
        this.appsid = appsid;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public DataBean getData() {
        return data;
    }

    public void setData(DataBean data) {
        this.data = data;
    }

}
