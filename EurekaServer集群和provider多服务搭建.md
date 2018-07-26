## 代码管理

　　我们通过 Git 来管理项目代码，借助 GitHub 平台来托管项目，同时通过如下 alias 可以在服务器上直接克隆安装项目。

```bash

    # 在 /etc/bashrc 中配置 alias
    alias ins='function __ins() { local d=$(pwd); local branch="master"; if [ $# -eq 1 ]; then branch="$1"; fi; echo -en "${green}安装microservice的${branch}分支${endColor}\n"; cd /opt/code; rm -rf microservice; git clone -b ${branch} https://github.com/RekaDowney/microservice.git; cd microservice; mvn clean install; cd "${d}"; unset -f __ins; }; __ins'
    
    # 将 ins 这个 alias 解构之后，得到如下函数
    
    function __ins() {
        # 执行函数时的当前目录
        local d=$(pwd)
        # 默认为 master 分支
        local branch="master"
        if [ $# -eq 1 ]; then
            # 如果传入了一个参数，那么将该参数作为要安装的项目分支名称
            branch="$1"
        fi
        echo -en "${green}安装microservice的${branch}分支${endColor}\n"
        # 进入代码存放目录
        cd /opt/code
        # 删除可能存在的 microservice 项目代码
        rm -rf microservice
        # 克隆指定分支的 microservice 项目到当前目录
        ## 特别注意：这里只能使用 https 协议，如果使用 git 协议会报没有权限的错误（想要使用 git 协议需要为每台服务器创建各自的公钥密钥，并将公钥添加到项目中或者创建 GitHub 账号后将账号加入到项目协作者里）
        git clone -b ${branch} https://github.com/RekaDowney/microservice.git
        # 进入项目根目录
        cd microservice
        #  执行 maven 安装命令
        mvn clean install
        # 回到执行函数前的目录
        cd "${d}"
        # 删除当前函数
        unset -f __ins
    }

```

　　通过 source /etc/bashrc 之后，我们可以直接使用 ins 命令安装 microservice 项目的 master 分支，或者通过 ins ${branchName} 命令安装 microservice 项目的 ${branchName} 分支。

## EurekaServer 集群管理

　　这边以三台服务器（三台服务器分别以 jd.me、tx.me、bd.me 为名）作为 EurekaServer 集群的搭建环境。通过 /etc/hosts 文件避免主机 IP 泄露，在 /etc/hosts 文件中有如下配置

### jd.me 服务器

　　针对 jd.me 服务器，部署 eureka7001 项目，其 /etc/hosts 配置文件如下：

```text

    # 本机采用 127.0.0.1
    127.0.0.1 eureka7001
    # 相对应的两台 eureka-server 服务器采用公网 IP 映射 
    *.*.*.* eureka7002
    *.*.*.* eureka7003

    # 采用公网 IP 映射，这里不采用 127.0.0.1 映射本机
    *.*.*.* jd.me
    *.*.*.* tx.me
    *.*.*.* bd.me

```

　　由于采用了不同服务器独立部署不同项目的方式，因此每个项目都采用了各自的配置文件，不需要再通过外部配置文件方式就可以直接启动项目。（后面 provider 服务将通过相同项目不同外部配置文件方式启动）
　　其启动命令为 nohup java -jar /path/to/eureka-server7001-1.0.jar > /path/to/log 2>&1 &

### tx.me 服务器

　　针对 tx.me 服务器，部署 eureka7002 项目，其 /etc/hosts 配置文件如下：

```text

    # 本机采用 127.0.0.1
    127.0.0.1 eureka7002
    # 相对应的两台 eureka-server 服务器采用公网 IP 映射 
    *.*.*.* eureka7001
    *.*.*.* eureka7003

    *.*.*.* jd.me
    # 采用公网 IP 映射，这里不采用 127.0.0.1 映射本机
    *.*.*.* tx.me
    *.*.*.* bd.me

```

　　其启动命令为 nohup java -jar /path/to/eureka-server7002-1.0.jar > /path/to/log 2>&1 &

### bd.me 服务器

　　针对 bd.me 服务器，部署 eureka7003 项目，其 /etc/hosts 配置文件如下：

```text

    # 本机采用 127.0.0.1
    127.0.0.1 eureka7003
    # 相对应的两台 eureka-server 服务器采用公网 IP 映射 
    *.*.*.* eureka7001
    *.*.*.* eureka7002

    *.*.*.* jd.me
    *.*.*.* tx.me
    # 采用公网 IP 映射，这里不采用 127.0.0.1 映射本机
    *.*.*.* bd.me

```

　　其启动命令为 nohup java -jar /path/to/eureka-server7001-1.0.jar > /path/to/log 2>&1 &

### eureka-server 脚本

　　可以很清楚地发现，上面三台服务器的 eureka-server 启动方式实际上有很多相似之处，因此可以参数抽象并脚本化，有如下脚本：

```bash

    # 在 /etc/profile 配置文件中已经导出了如下几个颜色控制变量
    export red='\E[1;31m'
    export green='\E[1;32m'
    export yellow='\E[1;33m'
    export blue='\E[1;34m'
    export pink='\E[1;35m'
    export endColor='\E[0m'
    
    # eureka-server 脚本
    # eureka 服务名称
    readonly eureka='eureka-server7001'
    readonly version='1.0'
    readonly eurekaJar="/opt/app/${eureka}-${version}.jar"
    readonly eurekaSourceJar="/opt/env/maven/repository/me/junbin/microservice/${eureka}/${version}/${eureka}-${version}.jar"
    readonly logFile="/opt/app/${eureka}.log"
    readonly scriptName=$(pwd)/$(basename $0)
    
    function exist() {
        local allps=$(ps auxw | grep ${eureka} | grep -v grep | wc -l)
        if [[ ${allps} -eq 1 ]]; then
            local pid=$(ps auxw | grep ${eureka} | grep -v grep | awk '{print $2}')
            if [[ ${pid} -gt 0 ]]; then
                 return 0
            fi
        fi
        return 1
    }
    
    function __start() {
        if exist ; then
            local pid=$(ps auxw | grep ${eureka} | grep -v grep | awk '{print $2}')
            echo -en "${red}${eureka}服务正在运行（pid=${pid}）...无法执行启动操作${endColor}\n"
            return 1
        fi
        rm -rf ${eurekaJar}
        cp ${eurekaSourceJar} ${eurekaJar}
        nohup java -jar ${eurekaJar} > ${logFile} 2>&1 &
        sleep 1s
        if exist ; then
            echo -en "${green}${eureka}服务启动成功...${endColor}\n"
            return 0
        else    
            echo -en "${red}${eureka}服务启动失败...${endColor}\n"
            return 1
        fi
    }
    
    function __stop() {
        if exist ; then
            local pid=$(ps auxw | grep ${eureka} | grep -v grep | awk '{print $2}')
            kill -KILL ${pid}
            if [[ $? -eq 0 ]]; then
                echo -en "${green}${eureka}服务关闭成功${endColor}\n"
                return 0
            else
                echo -en "${red}${eureka}服务关闭失败${endColor}\n"
                return 1
            fi
        else
            echo -en "${yellow}${eureka}服务尚未启动，无需关闭${endColor}\n"
            return 2
        fi
    }
    
    function __restart() {
        if ! exist ; then
            echo -en "${red}${eureka}服务尚未启动，无法重启${endColor}\n"
            return 10
        fi
        __stop > /dev/null 2>&1
        if [[ $? -ne 0 ]]; then
            echo -en "${red}${eureka}重启时服务关闭失败...${endColor}\n"
            return 11
        fi
        __start &>/dev/null
        if [[ $? -ne 0 ]]; then
            echo -en "${red}${eureka}重启时服务启动失败...${endColor}\n"
            return 12
        fi
        echo -en "${green}${eureka}服务重启成功...${endColor}\n"
        return 0
    }
    
    function __run() {
        if exist ; then
            echo -en "${yellow}${eureka}服务正在运行...${endColor}\n"
            return 3
        else
            __start &>/dev/null
            if [[ $? -ne 0 ]]; then
                echo -en "${red}${eureka}服务启动失败...${endColor}\n"
                return 0
            else
                echo -en "${green}${eureka}服务启动成功...${endColor}\n"
                return 1
            fi
        fi
    }
    
    function main() {
        if [[ $# -ne 1 ]]; then
            echo -en "${red}用法：${scriptName} <[start|stop|restart|run]>${endColor}\n"
            return 1
        fi
        local d=$(pwd)
        local result=0
        case "$1" in
            "start")
                __start
                result=$?
                ;;
            "stop")
                __stop
                result=$?
                ;;
            "restart")
                __restart
                result=$?
                ;;
            "run")
                __run
                result=$?
                ;;
            *)
                echo -en "${red}用法：${scriptName} <[start|stop|restart|run]>${endColor}\n"
                result=1
                ;;
        esac
        cd ${d}
        return ${result}
    }
    
    main $*
    exit $?

```

　　然后在 /etc/bashrc 文件中添加如下 alias

```bash

    alias eureka='/opt/app/scripts/eureka-server.sh'

```

　　通过 source /etc/bashrc 之后获得了如下快捷方式

```bash
    
    ## 启动 eureka-server，如果 eureka-server 已经启动则提示错误
    eureka start

    ## 关闭 eureka-server，如果 eureka-server 已经关闭则提示错误
    eureka stop
    
    ## 运行 eureka-server，如果 eureka-server 正在运行则提示正在运行，否则启动 eureka-server
    eureka run
    
    ## 重启 eureka-server，如果 eureka-server 已经启动则提示错误
    eureka restart

```

　　结合 SecureCRT 的 "Send Chat to All Sessions" 功能，可以一次性操作三台服务器管理 eureka-server。

## provider 服务

　　上面的 EurekaServer 集群采用每台服务器部署不同的项目，维护成本太高了。这里我们通过外部配置文件的方式，将 provider 模块以不同配置文件作为启动配置。借此避免出现多个 provider 模块，同时更加符合微服务的理念，为后续使用 Docker 打下基础（虽然后续会通过 spring-cloud-config 来统一管理配置）。

### provider 脚本

```bash

    # 服务名称
    readonly provider='provider8001'
    readonly version='1.0'
    readonly providerJar="/opt/app/provider/${provider}-${version}.jar"
    readonly providerSourceJar="/opt/env/maven/repository/me/junbin/microservice/${provider}/${version}/${provider}-${version}.jar"
    readonly logFile="/opt/app/provider/${provider}.log"
    readonly scriptName=$(pwd)/$(basename $0)
    readonly springbootConfigFile='/opt/app/provider/application.yml'
    
    function exist() {
        local allps=$(ps auxw | grep ${provider} | grep -v grep | wc -l)
        if [[ ${allps} -eq 1 ]]; then
            local pid=$(ps auxw | grep ${provider} | grep -v grep | awk '{print $2}')
            if [[ ${pid} -gt 0 ]]; then
                 return 0
            fi
        fi
        return 1
    }
    
    function __start() {
        if exist ; then
            local pid=$(ps auxw | grep ${provider} | grep -v grep | awk '{print $2}')
            echo -en "${red}${provider}服务正在运行（pid=${pid}）...无法执行启动操作${endColor}\n"
            return 1
        fi
        rm -rf ${providerJar}
        cp ${providerSourceJar} ${providerJar}
        # 方案一：进入到 SpringBoot ExecutableJar 所在目录，在该目录下添加 application.yml 或者 application.properties
        # 随后在该目录下启动 SpringBoot，此时会正确加载该配置文件
        cd $(dirname ${providerJar})
        nohup java -jar $(basename ${providerJar}) > ${logFile} 2>&1 &
        # 方案二：使用 spring.config.additional-location 增强配置文件加载路径
        # nohup java -jar ${providerJar} --spring.config.additional-location=file:${springbootConfigFile} > ${logFile} 2>&1 &
        sleep 1s
        if exist ; then
            echo -en "${green}${provider}服务启动成功...${endColor}\n"
            return 0
        else    
            echo -en "${red}${provider}服务启动失败...${endColor}\n"
            return 1
        fi
    }
    
    function __stop() {
        if exist ; then
            local pid=$(ps auxw | grep ${provider} | grep -v grep | awk '{print $2}')
            kill -KILL ${pid}
            if [[ $? -eq 0 ]]; then
                echo -en "${green}${provider}服务关闭成功${endColor}\n"
                return 0
            else
                echo -en "${red}${provider}服务关闭失败${endColor}\n"
                return 1
            fi
        else
            echo -en "${yellow}${provider}服务尚未启动，无需关闭${endColor}\n"
            return 2
        fi
    }
    
    function __restart() {
        if ! exist ; then
            echo -en "${red}${provider}服务尚未启动，无法重启${endColor}\n"
            return 10
        fi
        __stop > /dev/null 2>&1
        if [[ $? -ne 0 ]]; then
            echo -en "${red}${provider}重启时服务关闭失败...${endColor}\n"
            return 11
        fi
        __start &>/dev/null
        if [[ $? -ne 0 ]]; then
            echo -en "${red}${provider}重启时服务启动失败...${endColor}\n"
            return 12
        fi
        echo -en "${green}${provider}服务重启成功...${endColor}\n"
        return 0
    }
    
    function __run() {
        if exist ; then
            echo -en "${yellow}${provider}服务正在运行...${endColor}\n"
            return 3
        else
            __start &>/dev/null
            if [[ $? -ne 0 ]]; then
                echo -en "${red}${provider}服务启动失败...${endColor}\n"
                return 0
            else
                echo -en "${green}${provider}服务启动成功...${endColor}\n"
                return 1
            fi
        fi
    }
    
    function main() {
        if [[ $# -ne 1 ]]; then
            echo -en "${red}用法：${scriptName} <[start|stop|restart|run]>${endColor}\n"
            return 1
        fi
        local d=$(pwd)
        local result=0
        case "$1" in
            "start")
                __start
                result=$?
                ;;
            "stop")
                __stop
                result=$?
                ;;
            "restart")
                __restart
                result=$?
                ;;
            "run")
                __run
                result=$?
                ;;
            *)
                echo -en "${red}用法：${scriptName} <[start|stop|restart|run]>${endColor}\n"
                result=1
                ;;
        esac
        cd "${d}"
        return ${result}
    }
    
    main $*
    exit $?

```

　　这里需要注意的是，启动 springboot 的时候，默认是从执行 java -jar /path/to/${project}.jar 命令的当前目录下加载 application.yml 或者 application.properties 外部配置文件的，而不是在 ${project}.jar 文件所在的目录下加载配置文件。

　　因此为了加载 ${project}.jar 文件所在目录下的配置文件，有如下两种解决方案：

　　一：进入到 ${project}.jar 所在目录，在该目录下启动项目；
　　二：执行启动命令时，添加 --spring.config.additional-location=/path/to/xxx.yml|xxx.properties 参数指定配置文件的绝对路径。（通常不建议使用 spring.config.location 参数）

　　有了这个脚本，我们可以为该脚本添加一条 alias

```bash

    alias provider='/opt/app/scripts/provider.sh'    

```

　　通过 source /etc/bashrc 之后获得了如下快捷方式

```bash
    
    ## 启动 provider，如果 provider 已经启动则提示错误
    provider start

    ## 关闭 provider，如果 provider 已经关闭则提示错误
    provider stop
    
    ## 运行 provider，如果 provider 正在运行则提示正在运行，否则启动 provider
    provider run
    
    ## 重启 provider，如果 provider 已经启动则提示错误
    provider restart

```

　　接着是三台服务器上对 provider 模块的配置

### jd.me 服务器

```yaml

    eureka:
      instance:
        # 指定实例名称
        instance-id: jd_provider8001
        # 指定 IP 地址（客户端需要绑定 host）
        ip-address: jd.me
    
    jdbc:
      # 指定数据库连接
      url: jdbc:mysql://tx.me:3846/microservice01?verifyServerCertificate=false&useSSL=true

```

### tx.me 服务器

```yaml

    eureka:
      instance:
        # 指定实例名称
        instance-id: tx_provider8001
        # 指定 IP 地址（这里使用了公网IP地址）
        ip-address: *.*.*.*
    
    jdbc:
      # 指定数据库连接
      url: jdbc:mysql://tx.me:3846/microservice02?verifyServerCertificate=false&useSSL=true

```

### bd.me 服务器

```yaml

    eureka:
      instance:
        # 指定实例名称
        instance-id: bd_provider8001
        # 指定 IP 地址（客户端需要绑定 host）
        ip-address: bd.me
    
    jdbc:
      # 指定数据库连接
      url: jdbc:mysql://tx.me:3846/microservice03?verifyServerCertificate=false&useSSL=true

```

## 总结

　　通过上面的配置与脚本，我们可以快速地在本地开发，然后推送代码到 GitHub，随后借助 alias 快速实现 eureka-server 集群搭建，provider 服务注册等功能。
