package org.mainapp;

enum MainAPIServer {
    mongodb,
    mysql,
    offline
};

public class GlobalConst {

    // 得到的返回 JSON 长这样：
    /**
     * req.data:
     * data: {
     * qqAuth: String,
     * redirectLink: String
     * }
     */
    static String OAuthURL = "https://api.mxowl.com/qq-auth/go?fire-from-app=jtodo";

    static String defaultAvatarUrl = "https://app.mxowl.com/pwa-assets/manifest-icon-192.maskable.png";

    /**
     * 注意：*.dev.mxowl.com 域名用的证书在 Java 自带的 cacerts 文件中不存在，需要手动添加。该操作是一次性的
     * <p>
     * C:\Program Files\Java\jdk-17.0.2\bin\keytool.exe             ↓ 更换为你的 Java 目录
     * -import -alias example -keystore "C:\Program Files\Java\jdk-17.0.2\lib\security\cacerts"
     * -file .\org\mainapp\resources\dev.mxowl.com.cer
     * <p>
     * 或者，手动将 org/resources 文件夹内的已准备好的了的 cacerts 文件复制到 Java 目录下的 lib/security/ 文件夹中，替换原有的 cacerts 文件
     * <p>
     * [2022-06-19] 请留意: SSL 证书有效期不保证长期有效。此项目目录中存储的证书的有效期为 2022-06-03 至 2022-09-02
     * 你可以对代码进行一些修改，以便用自己的后端服务来驱动此应用。
     */
    static String mainAPIUrlPrefix = "https://jtodo.dev.mxowl.com";

    static MainAPIServer mainAPIServer = MainAPIServer.mongodb;

    static void switchMainAPIServer(MainAPIServer server) {
        mainAPIServer = server;
        switch (server) {
            case mongodb:
                mainAPIUrlPrefix = "https://jtodo.dev.mxowl.com";
                break;
            case mysql:
                mainAPIUrlPrefix = "https://sqltodo.dev.mxowl.com";
                break;
            case offline:
                mainAPIUrlPrefix = "http://localhost:8080";
                break;
        }
    }

    // POST, body: { "qqAuth": String }
    static String getFinishAuthenticationURL() {
        return mainAPIUrlPrefix + "/auth";
    }

    // GET,D body: { "code": "ok", "data": { "collections": [], "items": [] } }
    static String getInitialGetDataUrl() {
        return mainAPIUrlPrefix + "/data";
    }

    // method: PATCH
    static String getSyncUrl() {
        return mainAPIUrlPrefix + "/data/sync";
    }


    public static String versionText = "Alpha 0.2.0 (2022-06-22)";
    public static String updateLog = """
            Alpha 0.2.0
            - 可以选择连接的服务器了 (有两种后端服务：MongoDB 和 MySQL)
            - 可以以离线模式启动了

            Alpha 0.1.8
            - 修复主界面窗口在调整大小时，底栏的显示问题

            Alpha 0.1.7
            - 修复了查看已有定时的任务详情时，已设置的提醒时间不会正确显示的问题

            Alpha 0.1.6
            - 可以在菜单中设置音效开关了
            - 可以在菜单中手动重新加载了
            - 可以在菜单中设置是否要隐藏已完成的任务了
            - 代办列表中，可以显示每张列表的未完成任务数量了

            Alpha 0.1.5
            - 现在，完成任务时会有音效了

            Alpha 0.1.4
            - 调整了任务提醒窗口的样式和文案
            - 现在会有声音提醒了

            Alpha 0.1.3
            - 定时任务可以正常使用了
            - 窗口会在内容调整时重新居中了
            - 修正部分体验问题

            Alpha 0.1.2
            - 可以修改和删除 Collection 了
            - 界面总体的配色倾向稍微统一了一下
            - 任务不再按照字典序排序了

            Alpha 0.1.1
            - 主界面的任务和列表项目溢出时可以滚动了
            - 可以为任务添加说明文字了
            - 为单个任务添加了管理窗口，可以修改已有信息

            Alpha 0.1.0
            - 添加关于页面、更新日志页面
            - 可以在应用程序的菜单中查看这些页面了
            - 为登录界面也添加了菜单条
            - 窗口可以正确居中了 (在 pack() 之后调用 setLocationRelativeTo(parent))

            Alpha 0.0.9
            - 可以删除添加的项目了
            - 删除项目的同步功能可用了

            Alpha 0.0.8
            - 添加的列表和事项可以被正确渲染了
            - 事项会按照字典序进行排序，已完成的事项总会排在后面

            Alpha 0.0.7
            - 新增列表、新增事件可以同步了
            - 同步消息可以显示同步队列中的项目数量
            - 同步事件有了异常接管，遇到网络问题会丢弃超时或错误的请求、并在下一次重试

            Alpha 0.0.6
            - 应用程序的主界面基本结构绘制完成了
            - 窗口可以居中启动了

            Alpha 0.0.5
            - 异步网络请求模块测试
            - JSON 封装 Request Body 效果测试

            Alpha 0.0.4
            - 同步的网络请求模块测试
            - 可以正确拉取用户初始数据了

            Alpha 0.0.3
            - 用户登录模块测试通过
            - 异常接管模块全部补全

            Alpha 0.0.2
            - 登录窗口绘制完毕了
            - 可以拉取登录请求的验证 Token 了

            Alpha 0.0.1
            - 程序总体结构确定
                        """;
}
