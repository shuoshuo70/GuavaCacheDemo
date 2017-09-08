Google :   Guava Cache   --- 本地缓存
1.	自动载入键值至缓存
2.	LRU策略
3.	过期规则根据读写时间
4.	设置键值引用级别
5.	元素移出通知
6.	缓存访问统计

创建的两种方式：
1.	New cacheLoader抽象类
2.	New callable接口
区别：cacheLoader是在创建cache的时候定义，callable可以在使用get的时候指定
一致：从缓存中取key X的值，如果该值已经缓存过了，则返回缓存中的值，如果没有缓存过，可以通过某个方法来获取这个值。

– 使用MapMaker类创建ConcurrentMap实例
– 使用CacheBuilder的链式编程的方式创建LoadingCache和Cache实例
– 使用CacheBuilderSpec类通过格式化的字符串创建CacheBuilder实例
– 使用LoadingCache的实例CacheLoader通过指定的key获取对应的值
– 使用CacheStats类查看Cache的使用状态
– 使用RemovalListener类接受一个entry从map中删除的事件


刷新缓存的方法
Refresh（key）


Interceptor 可用于校验，国际化和转换器，是AOP实现的一种策略。是action前后执行的对象。
HandlerInterceptorAdapter
Spring包的方法
可重写三个方法
PreHandle， postHandle， afterCompletion
在action的不同位置进行处理
在preHandle中，可以进行编码、安全控制等处理；
在postHandle中，有机会修改ModelAndView；
在afterCompletion中，可以根据ex是否为null判断是否发生了异常，进行日志记录。

永久登录：将密码加密以后把用户名和密码放在cookie中，并设置cookie的有效期，再下次访问的时候与db比较，到时只需要验证用户名与时间戳即可。


用户权限校验：
1.	通过handlerMethod得到要运行的controller菜单的@rightVerify注解获取RightVerifyAction
2.	通过RightVerifyAction获取这个菜单的menuId和menuIds。
在这个过程中，如果
1）	RightVerifyAction对象为空，说明此菜单不需要权限，都可以访问
2）	MenuId和menuIds都为空，说明用户未登录，跳转到登录页面
3）	在有值的情况下，将menuId和menuIds放入一个list中
3.	从cookie中寻找登录信息。
在这个过程中，如果
1）	cookie为空，或者cookie为携带值，则用户未登录，跳转至登录页面
2）	在用户登录的情况下，判断登录时间，看是否超时，超时重新登录
3）	否则，用户登录成功，并返回登录信息
4.	根据用户的登录信息进行权限校验。
1）	从guava cache中取用户权限，过程如下：
1.1）	根据用户名及用户更新时间生成key，在cache中取value
1.2）	根据用户更新时间和cache中更新时间，判断cache中的数据是否是最新的，若不是，刷新缓存，cache会从数据库中重新取数据，再从缓存中取一次，就是最新的了
2）	取出的权限如果为空，用户无权限，若有值，看menuId列表是否在用户权限中，若有则可以访问controller，否则拒绝访问