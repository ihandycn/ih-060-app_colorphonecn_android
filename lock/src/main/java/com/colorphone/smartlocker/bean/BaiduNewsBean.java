package com.colorphone.smartlocker.bean;

import android.support.annotation.Keep;

import java.util.List;

@Keep
public class BaiduNewsBean {
    @Keep
    public static class CatInfoBean {
        /**
         * id : 1019
         * name :
         */
        public int id;
        public String name;
    }

    @Keep
    public static class ExtBean {
        /**
         * outid : 9885833
         * wapUrl : https: //jingyan.baidu.com/ContentUnionWap/0bc808fc198a701bd585b94a.html
         */

        public String outid;
        public String wapUrl;
    }

    @Keep
    public static class DislikeReasonsBean {
        /**
         * id : A_2_1_68567417
         * reason :
         */

        public String id;
        public String reason;
    }

    @Keep
    public static class TagsBean {
        /**
         * id : 68567417
         * text :
         */

        public int id;
        public String text;
    }

    /**
     * bigPicUrl : //publish-pic-cpu.baidu.com/718b5dd9-6c47-4328-a219-79ac4594c7c1.jpeg@c_1, y_26, w_696, h_348
     * brief :
     * catInfo : {"id":1019,"name":""}
     * clickDc : https: //caclick.baidu.com/log.gif?baidu_id=422759635d76962c27b6c5e345d750a4&app_id=a5c8dfc0&ts=1547781180186&ip=109.105.23.23&log_type=pv&from=detail&req_id=7fe122c29be440949f6ec6602d40d18b&log_id=1547781179341af199e344ed79267d7e&page_id=10002&pv_id=fe6c08ae18d14f0c8e03c2e8765cd605&last_pv_id=ebda2b339eec418090a811cc730c47f2&app_type=h5&content_id=A26301016068637199&url_type=1&recommend_type=1&site_id=1&api_version=2&api_type=2&list_scene=0&forward=api&content_type=news&exp_infos=9243_9353_20211_21500_21501_21902_21953_21961_22645_23021_23631_23921_23966_25313_25412_29593_29886_29903_29912_20011&channel_id=1022&blockId=0&osid=2&redirector=1&view_url=https%3A%2F%2Fcpu.baidu.com%2Fapi%2F1022%2Fa5c8dfc0%2Fdetail%2F26301016068637199%2Fnews%3Fim%3DcgYTlS8N1x2-Sxxh6_HmytYQJkjgGP8_T1TjcWMgbBf_gCVcdMyONSfwxmV3TnugFo3QyBocB1cdUsgrbJ0q4G1EWQXPxQwtdb4KPXUdju9FRemce0bydzR8somQX8Ck6oPcJhxSjIx_Y6Hq9uXT61hzs2PS1aSw5yts4yUsI33JuMx1H3FT3TW0BRFQdIObLCDl8tSM_PH4q3PMwnck3HI3kJMQ_gqugUFxxgZQcx682k477a_3srI_qYx3cePd5jgQGV22WS6Hfq4IqM86jeS3pNgIlQV7GjpDbvYvcDHwBFb2R5h9s7lDTwHbphqPMK8bdjTgdotoZfXpnpkA%26aid%3DMx-QfQklgNVLFRRUq-_nhUoi08dtUSUO5Ml9zuTSqi88TfaTq-Y0EMdWp7uVpwRlxyv6nAhnYePbDSztP6pJEjh_QrXLCzaR7IaoA7t2BysTh4crsaJ0sEjUO1YvmnvV_scZF_cVOToYLxZaKMiFk59ukalVIJo_hFsNuxZGS_gmTOGu0Scd_Ntgwwn50lfv1y110EnACII6zAYKHymtV1l6jVTobQ48d73ptbDwlXHlN_WHw6X95u2TAYM5DUE65O-uyBqmoNzmqyU-6_KKiE0ptS76IHf9--s6p5TXaG4497kfFKMeaokyPWjtR9UHDnY1MQAWEHN7Rj-x61QA%26imMd5%3DkR81oeAzeUi5SRS8plEkyx08MHm7TOIe4COvJnaMtKssw94_V0ul3uYIULszN28c9JnZ4XcAxCF3pE2YeUKL8JeIkXBdDUrFfCnII0vCn0SWd4XQXZlat5mIAhmcAHd6G32sE9KUIYa1iXevCP243X36hdUO7mogHEmkbNWjmwR0H6i0nEcy75rRg3MD2D-q1L0UmS1aai2-ngr3bRDm43qfv1jeeKkU9PgiYrL3hlO0SuXR3Dgbkf32LEIp9IbZp_bbWpU80fyamPbic3C8DdaV7CoLB1FRdStIGjIC87nVUtXGQ4to0bFsXOyPCGrtlbx0b0X4TfDT-hO3xVIg%26cpid%3DrbXtQHGggVjsLBUic9cptVCL4aUPpFJkkuBgFf8_mzWBTm3NsZOXOZL4so8IKS8wOJoJcvBQxUMwHJTU2YmIv5W6rHol8ftqLNUikJcromKKCYVB755Dr03HH8hNuvgFvne0cp19M72UwfxzVQl5AkSLptFooNfc8GExvWsgOP0Xep9nKHr0Mmto4Sg3NcEjX7ISLQzyZR4B30EDMa44P1zSsyLKXbWiftM8ztZ8k_UHxub7A5Gd7o4Rn9oxKYe_cSlNt6PcBa6OxU08GUNE6Tn4Ay1U5_Xv5YmczJt2uh8f4mcWUdK5lCZ6vMlPusJwTs-QIv3456Wu69fSRJw%26scene%3D0%26log_id%3D1547781179341af199e344ed79267d7e%26exp_infos%3D9243_9353_20211_21500_21501_21902_21953_21961_22645_23021_23631_23921_23966_25313_25412_29593_29886_29903_29912_20011%26no_list%3D1%26forward%3Dapi%26api_version%3D2%26rt%3D11%26rts%3D2048&outer_id=null&entry=2&pattern=1&scene=0&ia=null&im=cgYTlS8N1x2-Sxxh6_HmytYQJkjgGP8_T1TjcWMgbBf_gCVcdMyONSfwxmV3TnugFo3QyBocB1cdUsgrbJ0q4G1EWQXPxQwtdb4KPXUdju9FRemce0bydzR8somQX8Ck6oPcJhxSjIx_Y6Hq9uXT61hzs2PS1aSw5yts4yUsI33JuMx1H3FT3TW0BRFQdIObLCDl8tSM_PH4q3PMwnck3HI3kJMQ_gqugUFxxgZQcx682k477a_3srI_qYx3cePd5jgQGV22WS6Hfq4IqM86jeS3pNgIlQV7GjpDbvYvcDHwBFb2R5h9s7lDTwHbphqPMK8bdjTgdotoZfXpnpkA&cpid=rbXtQHGggVjsLBUic9cptVCL4aUPpFJkkuBgFf8_mzWBTm3NsZOXOZL4so8IKS8wOJoJcvBQxUMwHJTU2YmIv5W6rHol8ftqLNUikJcromKKCYVB755Dr03HH8hNuvgFvne0cp19M72UwfxzVQl5AkSLptFooNfc8GExvWsgOP0Xep9nKHr0Mmto4Sg3NcEjX7ISLQzyZR4B30EDMa44P1zSsyLKXbWiftM8ztZ8k_UHxub7A5Gd7o4Rn9oxKYe_cSlNt6PcBa6OxU08GUNE6Tn4Ay1U5_Xv5YmczJt2uh8f4mcWUdK5lCZ6vMlPusJwTs-QIv3456Wu69fSRJw&aid=MxQfQklgNVLFRRUq-_nhUoi08dtUSUO5Ml9zuTSqi88TfaTq-Y0EMdWp7uVpwRlxyv6nAhnYePbDSztP6pJEjh_QrXLCzaR7IaoA7t2BysTh4crsaJ0sEjUO1YvmnvV_scZF_cVOToYLxZaKMiFk59ukalVIJo_hFsNuxZGS_gmTOGu0Scd_Ntgwwn50lfv1y110EnACII6zAYKHymtV1l6jVTobQ48d73ptbDwlXHlN_WHw6X95u2TAYM5DUE65O-uyBqmoNzmqyU-6_KKiE0ptS76IHf9--s6p5TXaG4497kfFKMeaokyPWjtR9UHDnY1MQAWEHN7Rj-x61QA&outeruid=null&rt=1.11&rts=1.2048
     * createTime : 2019-01-1521: 54: 45
     * detailUrl : https: //cpu.baidu.com/api/1022/a5c8dfc0/detail/26301016068637199/news?im=cgYTlS8N1x2-Sxxh6_HmytYQJkjgGP8_T1TjcWMgbBf_gCVcdMyONSfwxmV3TnugFo3QyBocB1cdUsgrbJ0q4G1EWQXPxQwtdb4KPXUdju9FRemce0bydzR8somQX8Ck6oPcJhxSjIx_Y6Hq9uXT61hzs2PS1aSw5yts4yUsI33JuMx1H3FT3TW0BRFQdIObLCDl8tSM_PH4q3PMwnck3HI3kJMQ_gqugUFxxgZQcx682k477a_3srI_qYx3cePd5jgQGV22WS6Hfq4IqM86jeS3pNgIlQV7GjpDbvYvcDHwBFb2R5h9s7lDTwHbphqPMK8bdjTgdotoZfXpnpkA&aid=Mx-QfQklgNVLFRRUq-_nhUoi08dtUSUO5Ml9zuTSqi88TfaTq-Y0EMdWp7uVpwRlxyv6nAhnYePbDSztP6pJEjh_QrXLCzaR7IaoA7t2BysTh4crsaJ0sEjUO1YvmnvV_scZF_cVOToYLxZaKMiFk59ukalVIJo_hFsNuxZGS_gmTOGu0Scd_Ntgwwn50lfv1y110EnACII6zAYKHymtV1l6jVTobQ48d73ptbDwlXHlN_WHw6X95u2TAYM5DUE65O-uyBqmoNzmqyU-6_KKiE0ptS76IHf9--s6p5TXaG4497kfFKMeaokyPWjtR9UHDnY1MQAWEHN7Rjx61QA&imMd5=kR81oeAzeUi5SRS8plEkyx08MHm7TOIe4COvJnaMtKssw94_V0ul3uYIULszN28c9JnZ4XcAxCF3pE2YeUKL8JeIkXBdDUrFfCnII0vCn0SWd4XQXZlat5mIAhmcAHd6G32sE9KUIYa1iXevCP243X36hdUO7mogHEmkbNWjmwR0H6i0nEcy75rRg3MD2D-q1L0UmS1aai2-ngr3bRDm43qfv1jeeKkU9PgiYrL3hlO0SuXR3Dgbkf32LEIp9IbZp_bbWpU80fyamPbic3C8DdaV7CoLB1FRdStIGjIC87nVUtXGQ4to0bFsXOyPCGrtlbx0b0X4TfDThO3xVIg&cpid=rbXtQHGggVjsLBUic9cptVCL4aUPpFJkkuBgFf8_mzWBTm3NsZOXOZL4so8IKS8wOJoJcvBQxUMwHJTU2YmIv5W6rHol8ftqLNUikJcromKKCYVB755Dr03HH8hNuvgFvne0cp19M72UwfxzVQl5AkSLptFooNfc8GExvWsgOP0Xep9nKHr0Mmto4Sg3NcEjX7ISLQzyZR4B30EDMa44P1zSsyLKXbWiftM8ztZ8k_UHxub7A5Gd7o4Rn9oxKYe_cSlNt6PcBa6OxU08GUNE6Tn4Ay1U5_Xv5YmczJt2uh8f4mcWUdK5lCZ6vMlPusJwTsQIv3456Wu69fSRJw&scene=0&log_id=1547781179341af199e344ed79267d7e&exp_infos=9243_9353_20211_21500_21501_21902_21953_21961_22645_23021_23631_23921_23966_25313_25412_29593_29886_29903_29912_20011&no_list=1&forward=api&api_version=2&rt=11&rts=2048
     * dislikeReasons : [{"id":"A_2_1_68567417","reason":""},{"id":"A_2_1_76777399","reason":""}]
     * ext : {"outid":"9885833","wapUrl":"https: //jingyan.baidu.com/ContentUnionWap/0bc808fc198a701bd585b94a.html"}
     * id : 26301016068637199
     * ifImageNews : 0
     * images : ["https: //publish-pic-cpu.baidu.com/754d5971-8a09-4cde-bbcc-054f2e19f0a5.png@w_228, h_152"]
     * isTop : 0
     * outerCate : /
     * readCounts : 5190
     * readDc : https: //caclick.baidu.com/log.gif?baidu_id=422759635d76962c27b6c5e345d750a4&event_id=100056&app_id=a5c8dfc0&ts=1547781180186&ip=109.105.23.23&log_type=pv&from=detail&req_id=352c4d0d16a84687a6acac69f8105b27&log_id=1547781179341af199e344ed79267d7e&page_id=10002&pv_id=87ff7ca28bc54e2291f92e8a1cf88967&last_pv_id=ebda2b339eec418090a811cc730c47f2&app_type=h5&content_id=A26301016068637199&url_type=1&recommend_type=1&site_id=1&api_version=2&api_type=2&list_scene=0&forward=api&content_type=news&exp_infos=9243_9353_20211_21500_21501_21902_21953_21961_22645_23021_23631_23921_23966_25313_25412_29593_29886_29903_29912_20011&channel_id=1022&blockId=0&osid=2&redirector=1&view_url=https%3A%2F%2Fcpu.baidu.com%2Fapi%2F1022%2Fa5c8dfc0%2Fdetail%2F26301016068637199%2Fnews%3Fim%3DcgYTlS8N1x2-Sxxh6_HmytYQJkjgGP8_T1TjcWMgbBf_gCVcdMyONSfwxmV3TnugFo3QyBocB1cdUsgrbJ0q4G1EWQXPxQwtdb4KPXUdju9FRemce0bydzR8somQX8Ck6oPcJhxSjIx_Y6Hq9uXT61hzs2PS1aSw5yts4yUsI33JuMx1H3FT3TW0BRFQdIObLCDl8tSM_PH4q3PMwnck3HI3kJMQ_gqugUFxxgZQcx682k477a_3srI_qYx3cePd5jgQGV22WS6Hfq4IqM86jeS3pNgIlQV7GjpDbvYvcDHwBFb2R5h9s7lDTwHbphqPMK8bdjTgdotoZfXpnpkA%26aid%3DMx-QfQklgNVLFRRUq-_nhUoi08dtUSUO5Ml9zuTSqi88TfaTq-Y0EMdWp7uVpwRlxyv6nAhnYePbDSztP6pJEjh_QrXLCzaR7IaoA7t2BysTh4crsaJ0sEjUO1YvmnvV_scZF_cVOToYLxZaKMiFk59ukalVIJo_hFsNuxZGS_gmTOGu0Scd_Ntgwwn50lfv1y110EnACII6zAYKHymtV1l6jVTobQ48d73ptbDwlXHlN_WHw6X95u2TAYM5DUE65O-uyBqmoNzmqyU-6_KKiE0ptS76IHf9--s6p5TXaG4497kfFKMeaokyPWjtR9UHDnY1MQAWEHN7Rj-x61QA%26imMd5%3DkR81oeAzeUi5SRS8plEkyx08MHm7TOIe4COvJnaMtKssw94_V0ul3uYIULszN28c9JnZ4XcAxCF3pE2YeUKL8JeIkXBdDUrFfCnII0vCn0SWd4XQXZlat5mIAhmcAHd6G32sE9KUIYa1iXevCP243X36hdUO7mogHEmkbNWjmwR0H6i0nEcy75rRg3MD2D-q1L0UmS1aai2-ngr3bRDm43qfv1jeeKkU9PgiYrL3hlO0SuXR3Dgbkf32LEIp9IbZp_bbWpU80fyamPbic3C8DdaV7CoLB1FRdStIGjIC87nVUtXGQ4to0bFsXOyPCGrtlbx0b0X4TfDT-hO3xVIg%26cpid%3DrbXtQHGggVjsLBUic9cptVCL4aUPpFJkkuBgFf8_mzWBTm3NsZOXOZL4so8IKS8wOJoJcvBQxUMwHJTU2YmIv5W6rHol8ftqLNUikJcromKKCYVB755Dr03HH8hNuvgFvne0cp19M72UwfxzVQl5AkSLptFooNfc8GExvWsgOP0Xep9nKHr0Mmto4Sg3NcEjX7ISLQzyZR4B30EDMa44P1zSsyLKXbWiftM8ztZ8k_UHxub7A5Gd7o4Rn9oxKYe_cSlNt6PcBa6OxU08GUNE6Tn4Ay1U5_Xv5YmczJt2uh8f4mcWUdK5lCZ6vMlPusJwTs-QIv3456Wu69fSRJw%26scene%3D0%26log_id%3D1547781179341af199e344ed79267d7e%26exp_infos%3D9243_9353_20211_21500_21501_21902_21953_21961_22645_23021_23631_23921_23966_25313_25412_29593_29886_29903_29912_20011%26no_list%3D1%26forward%3Dapi%26api_version%3D2%26rt%3D11%26rts%3D2048&outer_id=null&entry=2&pattern=1&scene=0&ia=null&im=cgYTlS8N1x2-Sxxh6_HmytYQJkjgGP8_T1TjcWMgbBf_gCVcdMyONSfwxmV3TnugFo3QyBocB1cdUsgrbJ0q4G1EWQXPxQwtdb4KPXUdju9FRemce0bydzR8somQX8Ck6oPcJhxSjIx_Y6Hq9uXT61hzs2PS1aSw5yts4yUsI33JuMx1H3FT3TW0BRFQdIObLCDl8tSM_PH4q3PMwnck3HI3kJMQ_gqugUFxxgZQcx682k477a_3srI_qYx3cePd5jgQGV22WS6Hfq4IqM86jeS3pNgIlQV7GjpDbvYvcDHwBFb2R5h9s7lDTwHbphqPMK8bdjTgdotoZfXpnpkA&cpid=rbXtQHGggVjsLBUic9cptVCL4aUPpFJkkuBgFf8_mzWBTm3NsZOXOZL4so8IKS8wOJoJcvBQxUMwHJTU2YmIv5W6rHol8ftqLNUikJcromKKCYVB755Dr03HH8hNuvgFvne0cp19M72UwfxzVQl5AkSLptFooNfc8GExvWsgOP0Xep9nKHr0Mmto4Sg3NcEjX7ISLQzyZR4B30EDMa44P1zSsyLKXbWiftM8ztZ8k_UHxub7A5Gd7o4Rn9oxKYe_cSlNt6PcBa6OxU08GUNE6Tn4Ay1U5_Xv5YmczJt2uh8f4mcWUdK5lCZ6vMlPusJwTs-QIv3456Wu69fSRJw&aid=MxQfQklgNVLFRRUq-_nhUoi08dtUSUO5Ml9zuTSqi88TfaTq-Y0EMdWp7uVpwRlxyv6nAhnYePbDSztP6pJEjh_QrXLCzaR7IaoA7t2BysTh4crsaJ0sEjUO1YvmnvV_scZF_cVOToYLxZaKMiFk59ukalVIJo_hFsNuxZGS_gmTOGu0Scd_Ntgwwn50lfv1y110EnACII6zAYKHymtV1l6jVTobQ48d73ptbDwlXHlN_WHw6X95u2TAYM5DUE65O-uyBqmoNzmqyU-6_KKiE0ptS76IHf9--s6p5TXaG4497kfFKMeaokyPWjtR9UHDnY1MQAWEHN7Rjx61QA&outeruid=null&duration=0&rt=1.11&rts=1.2048
     * recommend : 0
     * reportUrl : https: //cpu.baidu.com/api/1022/a5c8dfc0/accuse/26301016068637199/news?sourceUrl=https%3A%2F%2Fcpu.baidu.com%2Fapi%2F1022%2Fa5c8dfc0%2Fdetail%2F26301016068637199%2Fnews%3Fim%3DcgYTlS8N1x2-Sxxh6_HmytYQJkjgGP8_T1TjcWMgbBf_gCVcdMyONSfwxmV3TnugFo3QyBocB1cdUsgrbJ0q4G1EWQXPxQwtdb4KPXUdju9FRemce0bydzR8somQX8Ck6oPcJhxSjIx_Y6Hq9uXT61hzs2PS1aSw5yts4yUsI33JuMx1H3FT3TW0BRFQdIObLCDl8tSM_PH4q3PMwnck3HI3kJMQ_gqugUFxxgZQcx682k477a_3srI_qYx3cePd5jgQGV22WS6Hfq4IqM86jeS3pNgIlQV7GjpDbvYvcDHwBFb2R5h9s7lDTwHbphqPMK8bdjTgdotoZfXpnpkA%26aid%3DMx-QfQklgNVLFRRUq-_nhUoi08dtUSUO5Ml9zuTSqi88TfaTq-Y0EMdWp7uVpwRlxyv6nAhnYePbDSztP6pJEjh_QrXLCzaR7IaoA7t2BysTh4crsaJ0sEjUO1YvmnvV_scZF_cVOToYLxZaKMiFk59ukalVIJo_hFsNuxZGS_gmTOGu0Scd_Ntgwwn50lfv1y110EnACII6zAYKHymtV1l6jVTobQ48d73ptbDwlXHlN_WHw6X95u2TAYM5DUE65O-uyBqmoNzmqyU-6_KKiE0ptS76IHf9--s6p5TXaG4497kfFKMeaokyPWjtR9UHDnY1MQAWEHN7Rj-x61QA%26imMd5%3DkR81oeAzeUi5SRS8plEkyx08MHm7TOIe4COvJnaMtKssw94_V0ul3uYIULszN28c9JnZ4XcAxCF3pE2YeUKL8JeIkXBdDUrFfCnII0vCn0SWd4XQXZlat5mIAhmcAHd6G32sE9KUIYa1iXevCP243X36hdUO7mogHEmkbNWjmwR0H6i0nEcy75rRg3MD2D-q1L0UmS1aai2-ngr3bRDm43qfv1jeeKkU9PgiYrL3hlO0SuXR3Dgbkf32LEIp9IbZp_bbWpU80fyamPbic3C8DdaV7CoLB1FRdStIGjIC87nVUtXGQ4to0bFsXOyPCGrtlbx0b0X4TfDT-hO3xVIg%26cpid%3DrbXtQHGggVjsLBUic9cptVCL4aUPpFJkkuBgFf8_mzWBTm3NsZOXOZL4so8IKS8wOJoJcvBQxUMwHJTU2YmIv5W6rHol8ftqLNUikJcromKKCYVB755Dr03HH8hNuvgFvne0cp19M72UwfxzVQl5AkSLptFooNfc8GExvWsgOP0Xep9nKHr0Mmto4Sg3NcEjX7ISLQzyZR4B30EDMa44P1zSsyLKXbWiftM8ztZ8k_UHxub7A5Gd7o4Rn9oxKYe_cSlNt6PcBa6OxU08GUNE6Tn4Ay1U5_Xv5YmczJt2uh8f4mcWUdK5lCZ6vMlPusJwTs-QIv3456Wu69fSRJw%26scene%3D0%26log_id%3D1547781179341af199e344ed79267d7e%26exp_infos%3D9243_9353_20211_21500_21501_21902_21953_21961_22645_23021_23631_23921_23966_25313_25412_29593_29886_29903_29912_20011%26no_list%3D1%26forward%3Dapi%26api_version%3D2%26rt%3D11%26rts%3D2048
     * showDc : https: //caclick.baidu.com/log.gif?site_id=1&event_id=100001&app_id=a5c8dfc0&channel_id=1022&entry=2&from=api&log_type=ev&req_id=ebda2b339eec418090a811cc730c47f20&pv_id=APIebda2b339eec418090a811cc730c47f2&page_id=60001&app_type=h5&api_type=2&pattern=1&scene=0&item_infos=1.A26301016068637199&rt=1.11&rts=1.2048&cluster_no=1.154764874011375&sourceType=21&log_id=1547781179341af199e344ed79267d7e&exp_infos=9243_9353_20211_21500_21501_21902_21953_21961_22645_23021_23631_23921_23966_25313_25412_29593_29886_29903_29912_20011&content_id=A26301016068637199&origin=1&aid=Mx-QfQklgNVLFRRUq-_nhUoi08dtUSUO5Ml9zuTSqi88TfaTqY0EMdWp7uVpwRlxyv6nAhnYePbDSztP6pJEjh_QrXLCzaR7IaoA7t2BysTh4crsaJ0sEjUO1YvmnvV_scZF_cVOToYLxZaKMiFk59ukalVIJo_hFsNuxZGS_gmTOGu0Scd_Ntgwwn50lfv1y110EnACII6zAYKHymtV1l6jVTobQ48d73ptbDwlXHlN_WHw6X95u2TAYM5DUE65O-uyBqmoNzmqyU-6_KKiE0ptS76IHf9--s6p5TXaG4497kfFKMeaokyPWjtR9UHDnY1MQAWEHN7Rjx61QA&cpid=rbXtQHGggVjsLBUic9cptVCL4aUPpFJkkuBgFf8_mzWBTm3NsZOXOZL4so8IKS8wOJoJcvBQxUMwHJTU2YmIv5W6rHol8ftqLNUikJcromKKCYVB755Dr03HH8hNuvgFvne0cp19M72UwfxzVQl5AkSLptFooNfc8GExvWsgOP0Xep9nKHr0Mmto4Sg3NcEjX7ISLQzyZR4B30EDMa44P1zSsyLKXbWiftM8ztZ8k_UHxub7A5Gd7o4Rn9oxKYe_cSlNt6PcBa6OxU08GUNE6Tn4Ay1U5_Xv5YmczJt2uh8f4mcWUdK5lCZ6vMlPusJwTsQIv3456Wu69fSRJw&im=cgYTlS8N1x2-Sxxh6_HmytYQJkjgGP8_T1TjcWMgbBf_gCVcdMyONSfwxmV3TnugFo3QyBocB1cdUsgrbJ0q4G1EWQXPxQwtdb4KPXUdju9FRemce0bydzR8somQX8Ck6oPcJhxSjIx_Y6Hq9uXT61hzs2PS1aSw5yts4yUsI33JuMx1H3FT3TW0BRFQdIObLCDl8tSM_PH4q3PMwnck3HI3kJMQ_gqugUFxxgZQcx682k477a_3srI_qYx3cePd5jgQGV22WS6Hfq4IqM86jeS3pNgIlQV7GjpDbvYvcDHwBFb2R5h9s7lDTwHbphqPMK8bdjTgdotoZfXpnpkA&imMd5=kR81oeAzeUi5SRS8plEkyx08MHm7TOIe4COvJnaMtKssw94_V0ul3uYIULszN28c9JnZ4XcAxCF3pE2YeUKL8JeIkXBdDUrFfCnII0vCn0SWd4XQXZ-lat5mIAhmcAHd6G32sE9KUIYa1iXevCP243X36hdUO7mogHEmkbNWjmwR0H6i0nEcy75rRg3MD2D-q1L0UmS1aai2-ngr3bRDm43qfv1jeeKkU9PgiYrL3hlO0SuXR3Dgbkf32LEIp9IbZp_bbWpU80fyamPbic3C8DdaV7CoLB1FRdStIGjIC87nVUtXGQ4to0bFsXOyPCGrtlbx0b0X4TfDThO3xVIg&baidu_id=422759635d76962c27b6c5e345d750a4
     * source : RSY_SXY
     * tags : [{"id":68567417,"text":""},{"id":76777399,"text":""}]
     * title :
     * updateTime : 2019-01-1623: 46: 54
     */

    public String bigPicUrl;
    public String brief;
    public CatInfoBean catInfo;
    public String clickDc;
    public String createTime;
    public String detailUrl;
    public ExtBean ext;
    public String id;
    public int ifImageNews;
    public int isTop;
    public String outerCate;
    public int readCounts;
    public String readDc;
    public int recommend;
    public String reportUrl;
    public String showDc;
    public String source;
    public String title;
    public String updateTime;
    public List<DislikeReasonsBean> dislikeReasons;
    public List<String> images;
    public List<TagsBean> tags;


}
