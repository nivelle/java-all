package com.nivelle.core.javacore.jdk.security;

/**
 * java安全管理器类
 *
 * @author fuxinzhong
 * @date 2020/11/02
 */
public class SecurityManagerDemo {

    public static void main(String[] args) {
        SecurityManager securityManager = new SecurityManager();
        System.setSecurityManager(securityManager);
        securityManager.checkPermission(new MyPermisision("myself permission"));
    }

    /**
     * //第一部分授权: 授权基于路径在"file:${{java.ext.dirs}}/*"的class和jar包，所有权限。
     * grant codeBase "file:${{java.ext.dirs}}/*" {
     *         permission java.security.AllPermission;
     * };
     *
     * // default permissions granted to all domains
     * //第二部分授权: 这是细粒度的授权，对某些资源的操作进行授权
     * grant {
     *         // Allows any thread to stop itself using the java.lang.Thread.stop()
     *         // method that takes no argument.
     *         // Note that this permission is granted by default only to remain
     *         // backwards compatible.
     *         // It is strongly recommended that you either remove this permission
     *         // from this policy file or further restrict it to code sources
     *         // that you specify, because Thread.stop() is potentially unsafe.
     *         // See the API specification of java.lang.Thread.stop() for more
     *         // information.
     *         permission java.lang.RuntimePermission "stopThread";
     *
     *         // allows anyone to listen on dynamic ports
     *         permission java.net.SocketPermission "localhost:0", "listen";
     *
     *         // "standard" properies that can be read by anyone
     *
     *         permission java.util.PropertyPermission "java.version", "read";
     *         permission java.util.PropertyPermission "java.vendor", "read";
     *         permission java.util.PropertyPermission "java.vendor.url", "read";
     *         permission java.util.PropertyPermission "java.class.version", "read";
     *         permission java.util.PropertyPermission "os.name", "read";
     *         permission java.util.PropertyPermission "os.version", "read";
     *         permission java.util.PropertyPermission "os.arch", "read";
     *         permission java.util.PropertyPermission "file.separator", "read";
     *         permission java.util.PropertyPermission "path.separator", "read";
     *         permission java.util.PropertyPermission "line.separator", "read";
     *
     *         permission java.util.PropertyPermission "java.specification.version", "read";
     *         permission java.util.PropertyPermission "java.specification.vendor", "read";
     *         permission java.util.PropertyPermission "java.specification.name", "read";
     *
     *         permission java.util.PropertyPermission "java.vm.specification.version", "read";
     *         permission java.util.PropertyPermission "java.vm.specification.vendor", "read";
     *         permission java.util.PropertyPermission "java.vm.specification.name", "read";
     *         permission java.util.PropertyPermission "java.vm.version", "read";
     *         permission java.util.PropertyPermission "java.vm.vendor", "read";
     *         permission java.util.PropertyPermission "java.vm.name", "read";
     *
     *         //自定义的取消所有授权
     *         permission java.security.AllPermission;
     * };
     */
}
