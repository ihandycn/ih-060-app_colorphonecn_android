package com.colorphone.smartlocker.bean;

import android.support.annotation.Keep;

import java.util.List;

@Keep
public class BaiduVideoBean {
    @Keep
    public static class CatInfoBean {
        /**
         * id : 1040
         * name : 舞蹈
         */

        public int id;
        public String name;
    }

    @Keep
    public static class SourceBean {
        /**
         * name : 芸姐谈小品
         */

        public String name;
    }

    @Keep
    public static class VideoSizeInfoBean {
        /**
         * hd : 5566650
         * sc : 9960120
         * sd : 4008076
         */

        public int hd;
        public int sc;
        public int sd;
    }

    @Keep
    public static class DislikeReasonsBean {
        /**
         * id : C_2_1_69084096
         * reason : 姑娘
         */

        public String id;
        public String reason;

    }

    @Keep
    public static class TagsBean {
        /**
         * id : 69084096
         * text : 姑娘
         */

        public int id;
        public String text;

    }

    /**
     * authorPage : https://cpu.baidu.com/api/1022/b35d77fb/profile/e88ab8e5a790e8b088e5b08fe59381/video?forward=api&api_version=2
     * avatar : //publish-pic-cpu.baidu.com/ee4e96a8-b11a-4471-b178-db826e767232.jpeg
     * brief : 美女青青广场舞《姑娘》，人美舞美，看得我都入迷了！
     * catInfo : {"id":1040,"name":"舞蹈"}
     * clickDc : https://caclick.baidu.com/log.gif?baidu_id=e97ea3ecc2156d5e2dad0844b911a658&app_id=b35d77fb&ts=1565593488081&ip=10.0.1.189&log_type=pv&req_id=f5d4b41aca664b058e2c620abb55a67b&log_id=1565593487945b3a9c0a596e6f09f780&page_id=50002&pv_id=741f7667448842f7b11b693e526dbbd6&last_pv_id=629533dcdd824c6980bae103fd045f51&app_type=h5&content_id=C55096777416&url_type=1&recommend_type=1&site_id=1&api_version=2&api_type=2&list_scene=0&from=api&content_type=video&exp_infos=9241_20211_20401_21022_21042_21113_21500_21501_21901_21951_21961_22645_22673_23021_23163_23502_23631_23743_23921_25313_25412_25822_29052_29598_29622_29721_29728_29733_29889_29903_29914_20011_44000_44022_43053_43063_44083_44093_44303_44513_44603_44801_44903_40001_40052_43043_43082_43112&channel_id=1022&blockId=0&osid=2&redirector=1&view_url=https%3A%2F%2Fcpu.baidu.com%2Fapi%2F1022%2Fb35d77fb%2Fdetail%2F55096777416%2Fvideo%3Fim%3DnIerbvy9RTohRyXOPTDz45bWMVsUFpFEkqTxb3PRfGLb1fGM5G_NfJb96fNkc6awsdKPhbNJ9z7_8CFL2k0TZGloXJkFeohYmB7oAZKjWoKUIAZYLiRcrvRadCNCYHGV2zykr_ybBVrtKiMV8aQ9NanRRMA5RB0OrkP7umZsInDr1MY9wmMe0BXVNF9944z2-2e9xNaQavDYRSi1yywk0Kwd2zWKePfFfQyUmQPfg5W98r0_xTPVXcrsa9ublRXZrCLXg7vr11LgPFDGHW_bped3xKmvCxtkDkpBvA6NHPQMiblwBqooZnEse38lrvb4NCS7bOkWLQDoLEdIp2QCJA%26aid%3DnRxkcVPQv1EGUcpp2xUpunwtDy9C41nTOw4Ol7zSxiVKCuJjO5dPYhKl44JdLuEvoitXS9mI4pRsO-qO881CjyyF41D4AnAclvZodnOWeV5hXmyzFQwfjKBlL8Ip3w8rf1CYYWy6iOpqIjKsRPQp-QRcoZ2sI2-n0iqOjV-dFoCWnAt-H_O2iHAKgPHwFlceN07Zty16r5Gjg4fPccD4jiTWRll0EFRB7EeJBUVPK32pes98mrc7TXC-65-7Y_DlcC1nvU59s3GbrIE-uUKN-Z5bDm6B_Tu3I-BEW6FFAS00xLoUYRfcL_aUERZ3qK1JiaUKLMPoYf1LWBQKCnn6LQ%26imMd5%3DqT1ppOngaA1eVDvSs26Fnq3seFfejwugPvl-3jhfcHA-mWhSzA5lI_ECjJWguRtyd-TkONl8rxNLGdCUCrQ3-drjxCBQb86N25X1zQoBmgFKBBqKXQC27lkM6kH2FBbD5ta6nOHBPrjeo1CMVd8WRp4_M_rZUiIUq1gpUKLVrTlQ48sfguYEPmbqrSB2mnPC3cuNJHNPXXGhVSuSRQJ-MHELAZ9FYG4_Rf9QiLogtB97soqJm_ruXhnVELHfdnqHdp7dvYuDT7RcE17sQEcE7KWIVGo-5zdAE171iiC23iXARBXEOptOJnd3csOYDBXDQuhUeGI1cLQOz2fwOQr-BA%26cpid%3DU0_DtGpQXDU6TdypG83YGWj09EmIO5PFMm9PzKXL4ZjKzM9rnt4A9iE4Ar3mb1nqKiN7FUxtBgleG043aaI7Df4eY5-PEThZ1iHDxDCMxlfm6jSeRFY7qZyWYR1u2PccuIb1q84m76-KnozfdxclpenQEygaxKt3FRTGipCsEbg4bE-q9_KV8VaTX00ChSuMWeRtfkTtIeq0irzdAvaq8-U7pyJG12LKEepllfEqvnSCk2iSFrHZBJ66YFd1kYb6wju3p6bsU4mbh2FtZuQyQkrt9fguEReeXEQcARMwLfoUpkMyvG6sejRH9CWBosEBL5BME4CjZcirV8ycd8oJcw%26scene%3D0%26log_id%3D1565593487945b3a9c0a596e6f09f780%26exp_infos%3D9241_20211_20401_21022_21042_21113_21500_21501_21901_21951_21961_22645_22673_23021_23163_23502_23631_23743_23921_25313_25412_25822_29052_29598_29622_29721_29728_29733_29889_29903_29914_20011_44000_44022_43053_43063_44083_44093_44303_44513_44603_44801_44903_40001_40052_43043_43082_43112%26no_list%3D1%26forward%3Dapi%26api_version%3D2%26cds_session_id%3D78254c3f9caa41c19c6ba6370dbe7e76%26sblogid%3D454534229%26cpu_union_id%3DIMEI_1028c85cacfccc351182a51e30501371%26rt%3D112%26rts%3D2097152&outer_id=&entry=2&pattern=1&scene=0&ia=&im=nIerbvy9RTohRyXOPTDz45bWMVsUFpFEkqTxb3PRfGLb1fGM5G_NfJb96fNkc6awsdKPhbNJ9z7_8CFL2k0TZGloXJkFeohYmB7oAZKjWoKUIAZYLiRcrvRadCNCYHGV2zykr_ybBVrtKiMV8aQ9NanRRMA5RB0OrkP7umZsInDr1MY9wmMe0BXVNF9944z2-2e9xNaQavDYRSi1yywk0Kwd2zWKePfFfQyUmQPfg5W98r0_xTPVXcrsa9ublRXZrCLXg7vr11LgPFDGHW_bped3xKmvCxtkDkpBvA6NHPQMiblwBqooZnEse38lrvb4NCS7bOkWLQDoLEdIp2QCJA&cpid=U0_DtGpQXDU6TdypG83YGWj09EmIO5PFMm9PzKXL4ZjKzM9rnt4A9iE4Ar3mb1nqKiN7FUxtBgleG043aaI7Df4eY5-PEThZ1iHDxDCMxlfm6jSeRFY7qZyWYR1u2PccuIb1q84m76-KnozfdxclpenQEygaxKt3FRTGipCsEbg4bE-q9_KV8VaTX00ChSuMWeRtfkTtIeq0irzdAvaq8-U7pyJG12LKEepllfEqvnSCk2iSFrHZBJ66YFd1kYb6wju3p6bsU4mbh2FtZuQyQkrt9fguEReeXEQcARMwLfoUpkMyvG6sejRH9CWBosEBL5BME4CjZcirV8ycd8oJcw&aid=nRxkcVPQv1EGUcpp2xUpunwtDy9C41nTOw4Ol7zSxiVKCuJjO5dPYhKl44JdLuEvoitXS9mI4pRsO-qO881CjyyF41D4AnAclvZodnOWeV5hXmyzFQwfjKBlL8Ip3w8rf1CYYWy6iOpqIjKsRPQp-QRcoZ2sI2-n0iqOjV-dFoCWnAt-H_O2iHAKgPHwFlceN07Zty16r5Gjg4fPccD4jiTWRll0EFRB7EeJBUVPK32pes98mrc7TXC-65-7Y_DlcC1nvU59s3GbrIE-uUKN-Z5bDm6B_Tu3I-BEW6FFAS00xLoUYRfcL_aUERZ3qK1JiaUKLMPoYf1LWBQKCnn6LQ&rt=1.112&rts=1.2097152&sblogid=454534229&cds_session_id=78254c3f9caa41c19c6ba6370dbe7e76
     * definition :
     * detailUrl : https://cpu.baidu.com/api/1022/b35d77fb/detail/55096777416/video?im=nIerbvy9RTohRyXOPTDz45bWMVsUFpFEkqTxb3PRfGLb1fGM5G_NfJb96fNkc6awsdKPhbNJ9z7_8CFL2k0TZGloXJkFeohYmB7oAZKjWoKUIAZYLiRcrvRadCNCYHGV2zykr_ybBVrtKiMV8aQ9NanRRMA5RB0OrkP7umZsInDr1MY9wmMe0BXVNF9944z2-2e9xNaQavDYRSi1yywk0Kwd2zWKePfFfQyUmQPfg5W98r0_xTPVXcrsa9ublRXZrCLXg7vr11LgPFDGHW_bped3xKmvCxtkDkpBvA6NHPQMiblwBqooZnEse38lrvb4NCS7bOkWLQDoLEdIp2QCJA&aid=nRxkcVPQv1EGUcpp2xUpunwtDy9C41nTOw4Ol7zSxiVKCuJjO5dPYhKl44JdLuEvoitXS9mI4pRsO-qO881CjyyF41D4AnAclvZodnOWeV5hXmyzFQwfjKBlL8Ip3w8rf1CYYWy6iOpqIjKsRPQp-QRcoZ2sI2-n0iqOjV-dFoCWnAt-H_O2iHAKgPHwFlceN07Zty16r5Gjg4fPccD4jiTWRll0EFRB7EeJBUVPK32pes98mrc7TXC-65-7Y_DlcC1nvU59s3GbrIE-uUKN-Z5bDm6B_Tu3I-BEW6FFAS00xLoUYRfcL_aUERZ3qK1JiaUKLMPoYf1LWBQKCnn6LQ&imMd5=qT1ppOngaA1eVDvSs26Fnq3seFfejwugPvl-3jhfcHA-mWhSzA5lI_ECjJWguRtyd-TkONl8rxNLGdCUCrQ3-drjxCBQb86N25X1zQoBmgFKBBqKXQC27lkM6kH2FBbD5ta6nOHBPrjeo1CMVd8WRp4_M_rZUiIUq1gpUKLVrTlQ48sfguYEPmbqrSB2mnPC3cuNJHNPXXGhVSuSRQJ-MHELAZ9FYG4_Rf9QiLogtB97soqJm_ruXhnVELHfdnqHdp7dvYuDT7RcE17sQEcE7KWIVGo-5zdAE171iiC23iXARBXEOptOJnd3csOYDBXDQuhUeGI1cLQOz2fwOQr-BA&cpid=U0_DtGpQXDU6TdypG83YGWj09EmIO5PFMm9PzKXL4ZjKzM9rnt4A9iE4Ar3mb1nqKiN7FUxtBgleG043aaI7Df4eY5-PEThZ1iHDxDCMxlfm6jSeRFY7qZyWYR1u2PccuIb1q84m76-KnozfdxclpenQEygaxKt3FRTGipCsEbg4bE-q9_KV8VaTX00ChSuMWeRtfkTtIeq0irzdAvaq8-U7pyJG12LKEepllfEqvnSCk2iSFrHZBJ66YFd1kYb6wju3p6bsU4mbh2FtZuQyQkrt9fguEReeXEQcARMwLfoUpkMyvG6sejRH9CWBosEBL5BME4CjZcirV8ycd8oJcw&scene=0&log_id=1565593487945b3a9c0a596e6f09f780&exp_infos=9241_20211_20401_21022_21042_21113_21500_21501_21901_21951_21961_22645_22673_23021_23163_23502_23631_23743_23921_25313_25412_25822_29052_29598_29622_29721_29728_29733_29889_29903_29914_20011_44000_44022_43053_43063_44083_44093_44303_44513_44603_44801_44903_40001_40052_43043_43082_43112&no_list=1&forward=api&api_version=2&cds_session_id=78254c3f9caa41c19c6ba6370dbe7e76&sblogid=454534229&cpu_union_id=IMEI_1028c85cacfccc351182a51e30501371&rt=112&rts=2097152
     * dislikeReasons : [{"id":"C_2_1_69084096","reason":"姑娘"},{"id":"C_2_1_70019139","reason":"青青广场舞"},{"id":"C_2_1_67753685","reason":"性感"}]
     * duration : 89
     * id : 55096777416
     * outerCate :
     * playCounts : 68334
     * presentationType : 0
     * readDc : https://caclick.baidu.com/log.gif?baidu_id=e97ea3ecc2156d5e2dad0844b911a658&event_id=100056&app_id=b35d77fb&ts=1565593488081&ip=10.0.1.189&log_type=pv&from=api&req_id=696b89a366b3413fb7cda2404648d08e&log_id=1565593487945b3a9c0a596e6f09f780&page_id=50002&pv_id=3b8ec2733fd64789bdf57e3ae48fd3af&last_pv_id=629533dcdd824c6980bae103fd045f51&app_type=h5&content_id=C55096777416&url_type=1&recommend_type=1&site_id=1&api_version=2&api_type=2&list_scene=0&forward=api&content_type=video&exp_infos=9241_20211_20401_21022_21042_21113_21500_21501_21901_21951_21961_22645_22673_23021_23163_23502_23631_23743_23921_25313_25412_25822_29052_29598_29622_29721_29728_29733_29889_29903_29914_20011_44000_44022_43053_43063_44083_44093_44303_44513_44603_44801_44903_40001_40052_43043_43082_43112&channel_id=1022&blockId=0&osid=2&redirector=1&view_url=%3Fim%3DnIerbvy9RTohRyXOPTDz45bWMVsUFpFEkqTxb3PRfGLb1fGM5G_NfJb96fNkc6awsdKPhbNJ9z7_8CFL2k0TZGloXJkFeohYmB7oAZKjWoKUIAZYLiRcrvRadCNCYHGV2zykr_ybBVrtKiMV8aQ9NanRRMA5RB0OrkP7umZsInDr1MY9wmMe0BXVNF9944z2-2e9xNaQavDYRSi1yywk0Kwd2zWKePfFfQyUmQPfg5W98r0_xTPVXcrsa9ublRXZrCLXg7vr11LgPFDGHW_bped3xKmvCxtkDkpBvA6NHPQMiblwBqooZnEse38lrvb4NCS7bOkWLQDoLEdIp2QCJA%26aid%3DnRxkcVPQv1EGUcpp2xUpunwtDy9C41nTOw4Ol7zSxiVKCuJjO5dPYhKl44JdLuEvoitXS9mI4pRsO-qO881CjyyF41D4AnAclvZodnOWeV5hXmyzFQwfjKBlL8Ip3w8rf1CYYWy6iOpqIjKsRPQp-QRcoZ2sI2-n0iqOjV-dFoCWnAt-H_O2iHAKgPHwFlceN07Zty16r5Gjg4fPccD4jiTWRll0EFRB7EeJBUVPK32pes98mrc7TXC-65-7Y_DlcC1nvU59s3GbrIE-uUKN-Z5bDm6B_Tu3I-BEW6FFAS00xLoUYRfcL_aUERZ3qK1JiaUKLMPoYf1LWBQKCnn6LQ%26imMd5%3DqT1ppOngaA1eVDvSs26Fnq3seFfejwugPvl-3jhfcHA-mWhSzA5lI_ECjJWguRtyd-TkONl8rxNLGdCUCrQ3-drjxCBQb86N25X1zQoBmgFKBBqKXQC27lkM6kH2FBbD5ta6nOHBPrjeo1CMVd8WRp4_M_rZUiIUq1gpUKLVrTlQ48sfguYEPmbqrSB2mnPC3cuNJHNPXXGhVSuSRQJ-MHELAZ9FYG4_Rf9QiLogtB97soqJm_ruXhnVELHfdnqHdp7dvYuDT7RcE17sQEcE7KWIVGo-5zdAE171iiC23iXARBXEOptOJnd3csOYDBXDQuhUeGI1cLQOz2fwOQr-BA%26cpid%3DU0_DtGpQXDU6TdypG83YGWj09EmIO5PFMm9PzKXL4ZjKzM9rnt4A9iE4Ar3mb1nqKiN7FUxtBgleG043aaI7Df4eY5-PEThZ1iHDxDCMxlfm6jSeRFY7qZyWYR1u2PccuIb1q84m76-KnozfdxclpenQEygaxKt3FRTGipCsEbg4bE-q9_KV8VaTX00ChSuMWeRtfkTtIeq0irzdAvaq8-U7pyJG12LKEepllfEqvnSCk2iSFrHZBJ66YFd1kYb6wju3p6bsU4mbh2FtZuQyQkrt9fguEReeXEQcARMwLfoUpkMyvG6sejRH9CWBosEBL5BME4CjZcirV8ycd8oJcw%26scene%3D0%26log_id%3D1565593487945b3a9c0a596e6f09f780%26exp_infos%3D9241_20211_20401_21022_21042_21113_21500_21501_21901_21951_21961_22645_22673_23021_23163_23502_23631_23743_23921_25313_25412_25822_29052_29598_29622_29721_29728_29733_29889_29903_29914_20011_44000_44022_43053_43063_44083_44093_44303_44513_44603_44801_44903_40001_40052_43043_43082_43112%26no_list%3D1%26forward%3Dapi%26api_version%3D2%26cds_session_id%3D78254c3f9caa41c19c6ba6370dbe7e76%26sblogid%3D454534229%26cpu_union_id%3DIMEI_1028c85cacfccc351182a51e30501371&outer_id=&entry=2&pattern=1&scene=0&ia=&im=nIerbvy9RTohRyXOPTDz45bWMVsUFpFEkqTxb3PRfGLb1fGM5G_NfJb96fNkc6awsdKPhbNJ9z7_8CFL2k0TZGloXJkFeohYmB7oAZKjWoKUIAZYLiRcrvRadCNCYHGV2zykr_ybBVrtKiMV8aQ9NanRRMA5RB0OrkP7umZsInDr1MY9wmMe0BXVNF9944z2-2e9xNaQavDYRSi1yywk0Kwd2zWKePfFfQyUmQPfg5W98r0_xTPVXcrsa9ublRXZrCLXg7vr11LgPFDGHW_bped3xKmvCxtkDkpBvA6NHPQMiblwBqooZnEse38lrvb4NCS7bOkWLQDoLEdIp2QCJA&cpid=U0_DtGpQXDU6TdypG83YGWj09EmIO5PFMm9PzKXL4ZjKzM9rnt4A9iE4Ar3mb1nqKiN7FUxtBgleG043aaI7Df4eY5-PEThZ1iHDxDCMxlfm6jSeRFY7qZyWYR1u2PccuIb1q84m76-KnozfdxclpenQEygaxKt3FRTGipCsEbg4bE-q9_KV8VaTX00ChSuMWeRtfkTtIeq0irzdAvaq8-U7pyJG12LKEepllfEqvnSCk2iSFrHZBJ66YFd1kYb6wju3p6bsU4mbh2FtZuQyQkrt9fguEReeXEQcARMwLfoUpkMyvG6sejRH9CWBosEBL5BME4CjZcirV8ycd8oJcw&aid=nRxkcVPQv1EGUcpp2xUpunwtDy9C41nTOw4Ol7zSxiVKCuJjO5dPYhKl44JdLuEvoitXS9mI4pRsO-qO881CjyyF41D4AnAclvZodnOWeV5hXmyzFQwfjKBlL8Ip3w8rf1CYYWy6iOpqIjKsRPQp-QRcoZ2sI2-n0iqOjV-dFoCWnAt-H_O2iHAKgPHwFlceN07Zty16r5Gjg4fPccD4jiTWRll0EFRB7EeJBUVPK32pes98mrc7TXC-65-7Y_DlcC1nvU59s3GbrIE-uUKN-Z5bDm6B_Tu3I-BEW6FFAS00xLoUYRfcL_aUERZ3qK1JiaUKLMPoYf1LWBQKCnn6LQ&outeruid=&duration=89&rt=1.112&rts=1.2097152&sblogid=454534229
     * reportUrl : https://cpu.baidu.com/api/1022/b35d77fb/accuse/55096777416/video?sourceUrl=https%3A%2F%2Fcpu.baidu.com%2Fapi%2F1022%2Fb35d77fb%2Fdetail%2F55096777416%2Fvideo%3Fim%3DnIerbvy9RTohRyXOPTDz45bWMVsUFpFEkqTxb3PRfGLb1fGM5G_NfJb96fNkc6awsdKPhbNJ9z7_8CFL2k0TZGloXJkFeohYmB7oAZKjWoKUIAZYLiRcrvRadCNCYHGV2zykr_ybBVrtKiMV8aQ9NanRRMA5RB0OrkP7umZsInDr1MY9wmMe0BXVNF9944z2-2e9xNaQavDYRSi1yywk0Kwd2zWKePfFfQyUmQPfg5W98r0_xTPVXcrsa9ublRXZrCLXg7vr11LgPFDGHW_bped3xKmvCxtkDkpBvA6NHPQMiblwBqooZnEse38lrvb4NCS7bOkWLQDoLEdIp2QCJA%26aid%3DnRxkcVPQv1EGUcpp2xUpunwtDy9C41nTOw4Ol7zSxiVKCuJjO5dPYhKl44JdLuEvoitXS9mI4pRsO-qO881CjyyF41D4AnAclvZodnOWeV5hXmyzFQwfjKBlL8Ip3w8rf1CYYWy6iOpqIjKsRPQp-QRcoZ2sI2-n0iqOjV-dFoCWnAt-H_O2iHAKgPHwFlceN07Zty16r5Gjg4fPccD4jiTWRll0EFRB7EeJBUVPK32pes98mrc7TXC-65-7Y_DlcC1nvU59s3GbrIE-uUKN-Z5bDm6B_Tu3I-BEW6FFAS00xLoUYRfcL_aUERZ3qK1JiaUKLMPoYf1LWBQKCnn6LQ%26imMd5%3DqT1ppOngaA1eVDvSs26Fnq3seFfejwugPvl-3jhfcHA-mWhSzA5lI_ECjJWguRtyd-TkONl8rxNLGdCUCrQ3-drjxCBQb86N25X1zQoBmgFKBBqKXQC27lkM6kH2FBbD5ta6nOHBPrjeo1CMVd8WRp4_M_rZUiIUq1gpUKLVrTlQ48sfguYEPmbqrSB2mnPC3cuNJHNPXXGhVSuSRQJ-MHELAZ9FYG4_Rf9QiLogtB97soqJm_ruXhnVELHfdnqHdp7dvYuDT7RcE17sQEcE7KWIVGo-5zdAE171iiC23iXARBXEOptOJnd3csOYDBXDQuhUeGI1cLQOz2fwOQr-BA%26cpid%3DU0_DtGpQXDU6TdypG83YGWj09EmIO5PFMm9PzKXL4ZjKzM9rnt4A9iE4Ar3mb1nqKiN7FUxtBgleG043aaI7Df4eY5-PEThZ1iHDxDCMxlfm6jSeRFY7qZyWYR1u2PccuIb1q84m76-KnozfdxclpenQEygaxKt3FRTGipCsEbg4bE-q9_KV8VaTX00ChSuMWeRtfkTtIeq0irzdAvaq8-U7pyJG12LKEepllfEqvnSCk2iSFrHZBJ66YFd1kYb6wju3p6bsU4mbh2FtZuQyQkrt9fguEReeXEQcARMwLfoUpkMyvG6sejRH9CWBosEBL5BME4CjZcirV8ycd8oJcw%26scene%3D0%26log_id%3D1565593487945b3a9c0a596e6f09f780%26exp_infos%3D9241_20211_20401_21022_21042_21113_21500_21501_21901_21951_21961_22645_22673_23021_23163_23502_23631_23743_23921_25313_25412_25822_29052_29598_29622_29721_29728_29733_29889_29903_29914_20011_44000_44022_43053_43063_44083_44093_44303_44513_44603_44801_44903_40001_40052_43043_43082_43112%26no_list%3D1%26forward%3Dapi%26api_version%3D2%26cds_session_id%3D78254c3f9caa41c19c6ba6370dbe7e76%26sblogid%3D454534229%26cpu_union_id%3DIMEI_1028c85cacfccc351182a51e30501371%26rt%3D112%26rts%3D2097152
     * showDc : https://caclick.baidu.com/log.gif?site_id=1&event_id=100001&app_id=b35d77fb&channel_id=1022&entry=2&from=api&log_type=ev&req_id=629533dcdd824c6980bae103fd045f515&pv_id=API629533dcdd824c6980bae103fd045f51&page_id=60001&app_type=h5&api_type=2&pattern=1&scene=0&item_infos=1.C55096777416&rt=1.112&rts=1.2097152&cluster_no=1.155220630366986&source_type=1&log_id=1565593487945b3a9c0a596e6f09f780&exp_infos=9241_20211_20401_21022_21042_21113_21500_21501_21901_21951_21961_22645_22673_23021_23163_23502_23631_23743_23921_25313_25412_25822_29052_29598_29622_29721_29728_29733_29889_29903_29914_20011_44000_44022_43053_43063_44083_44093_44303_44513_44603_44801_44903_40001_40052_43043_43082_43112&content_id=C55096777416&origin=1&aid=nRxkcVPQv1EGUcpp2xUpunwtDy9C41nTOw4Ol7zSxiVKCuJjO5dPYhKl44JdLuEvoitXS9mI4pRsO-qO881CjyyF41D4AnAclvZodnOWeV5hXmyzFQwfjKBlL8Ip3w8rf1CYYWy6iOpqIjKsRPQp-QRcoZ2sI2-n0iqOjV-dFoCWnAt-H_O2iHAKgPHwFlceN07Zty16r5Gjg4fPccD4jiTWRll0EFRB7EeJBUVPK32pes98mrc7TXC-65-7Y_DlcC1nvU59s3GbrIE-uUKN-Z5bDm6B_Tu3I-BEW6FFAS00xLoUYRfcL_aUERZ3qK1JiaUKLMPoYf1LWBQKCnn6LQ&cpid=U0_DtGpQXDU6TdypG83YGWj09EmIO5PFMm9PzKXL4ZjKzM9rnt4A9iE4Ar3mb1nqKiN7FUxtBgleG043aaI7Df4eY5-PEThZ1iHDxDCMxlfm6jSeRFY7qZyWYR1u2PccuIb1q84m76-KnozfdxclpenQEygaxKt3FRTGipCsEbg4bE-q9_KV8VaTX00ChSuMWeRtfkTtIeq0irzdAvaq8-U7pyJG12LKEepllfEqvnSCk2iSFrHZBJ66YFd1kYb6wju3p6bsU4mbh2FtZuQyQkrt9fguEReeXEQcARMwLfoUpkMyvG6sejRH9CWBosEBL5BME4CjZcirV8ycd8oJcw&im=nIerbvy9RTohRyXOPTDz45bWMVsUFpFEkqTxb3PRfGLb1fGM5G_NfJb96fNkc6awsdKPhbNJ9z7_8CFL2k0TZGloXJkFeohYmB7oAZKjWoKUIAZYLiRcrvRadCNCYHGV2zykr_ybBVrtKiMV8aQ9NanRRMA5RB0OrkP7umZsInDr1MY9wmMe0BXVNF9944z2-2e9xNaQavDYRSi1yywk0Kwd2zWKePfFfQyUmQPfg5W98r0_xTPVXcrsa9ublRXZrCLXg7vr11LgPFDGHW_bped3xKmvCxtkDkpBvA6NHPQMiblwBqooZnEse38lrvb4NCS7bOkWLQDoLEdIp2QCJA&imMd5=qT1ppOngaA1eVDvSs26Fnq3seFfejwugPvl-3jhfcHA-mWhSzA5lI_ECjJWguRtyd-TkONl8rxNLGdCUCrQ3-drjxCBQb86N25X1zQoBmgFKBBqKXQC27lkM6kH2FBbD5ta6nOHBPrjeo1CMVd8WRp4_M_rZUiIUq1gpUKLVrTlQ48sfguYEPmbqrSB2mnPC3cuNJHNPXXGhVSuSRQJ-MHELAZ9FYG4_Rf9QiLogtB97soqJm_ruXhnVELHfdnqHdp7dvYuDT7RcE17sQEcE7KWIVGo-5zdAE171iiC23iXARBXEOptOJnd3csOYDBXDQuhUeGI1cLQOz2fwOQr-BA&cpu_union_id=IMEI_1028c85cacfccc351182a51e30501371&baidu_id=e97ea3ecc2156d5e2dad0844b911a658&sblogid=454534229&cds_session_id=78254c3f9caa41c19c6ba6370dbe7e76
     * source : {"name":"芸姐谈小品"}
     * tags : [{"id":69084096,"text":"姑娘"},{"id":70019139,"text":"青青广场舞"},{"id":67753685,"text":"性感"}]
     * thumbUrl : //publish-pic-cpu.baidu.com/4a7b7346-58b4-478c-aab5-cc33ba233333.jpeg@w_888,h_498
     * title : 美女青青广场舞《姑娘》，人美舞美，看得我都入迷了！
     * updateTime : 2019-06-16 14:57:33
     * url :
     * videoSize : 4008
     * videoSizeInfo : {"hd":5566650,"sc":9960120,"sd":4008076}
     */

    public String authorPage;
    public String avatar;
    public String brief;
    public CatInfoBean catInfo;
    public String clickDc;
    public String definition;
    public String detailUrl;
    public int duration;
    public long id;
    public String outerCate;
    public int playCounts;
    public int presentationType;
    public String readDc;
    public String reportUrl;
    public String showDc;
    public SourceBean source;
    public String thumbUrl;
    public String title;
    public String updateTime;
    public String url;
    public int videoSize;
    public VideoSizeInfoBean videoSizeInfo;
    public List<DislikeReasonsBean> dislikeReasons;
    public List<TagsBean> tags;
}
