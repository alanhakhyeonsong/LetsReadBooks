# 9장. 퍼시스턴트 볼륨(PV)과 퍼시스턴트 볼륨 클레임(PVC)
앞 장까지 살펴봤던 디플로이먼트들은 모두 상태가 없는 애플리케이션이었다. 디플로이먼트의 각 포드는 별도의 데이터를 가지고 있지 않았으며, 단순히 요청에 대한 응답만을 반환했다.

하지만, 데이터베이스처럼 포드 내부에서 특정 데이터를 보유해야 하는, 상태가 있는 애플리케이션의 경우 데이터를 어떻게 관리할지 고민해야 한다. MySQL 디플로이먼트를 통해 포드를 생성했다 하더라도 MySQL 포드 내부에 저장된 데이터는 절대 영속적이지 않다. 디플로이먼트를 삭제하면 포드도 함께 삭제되고, 그와 동시에 포드의 데이터 또한 함께 삭제되기 때문이다.

이를 해결하기 위해선 **포드의 데이터를 영속적으로 저장하기 위한 방법이 필요하다.**  
Docker와 마찬가지로 쿠버네티스에서도 호스트에 위치한 디렉토리를 각 포드와 공유함으로써 데이터를 보존하는 것이 가능하다. 하지만 여러 개의 서버로 구성된 쿠버네티스와 같은 클러스터 환경에선 이 방법이 적합하지 않을 수 있다. 쿠버네티스는 워커 노드 중 하나를 선택해 포드를 할당하는데, 특정 노드에서만 데이터를 보관해 저장하면 포드가 다른 노드로 옮겨갔을 때 해당 데이터를 사용할 수 없게 된다.

<img width="679" alt="image" src="https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/7baf9296-81bd-4b01-b67e-42e8eaac9bf3">

이를 해결할 수 있는 일반적인 방법은 어느 노드에서도 접근해 사용할 수 있는 퍼시스턴트 볼륨을 사용하는 것이다. **퍼시스턴트 볼륨은 워커 노드들이 네트워크 상에서 스토리지를 마운트해 영속적으로 저장할 수 있는 볼륨을 의미한다.** 따라서 포드에 장애가 생겨 다른 노드로 옮겨가더라도 해당 노드에서 퍼시스턴트 볼륨에 네트워크로 연결해 데이터를 계속해서 사용할 수 있다.  
ex) NFS, AWS EBS, Ceph, GlusterFS 등

쿠버네티스는 퍼시스턴트 볼륨을 사용하기 위한 기능을 자체적으로 제공하고 있다.

## 로컬 볼륨: hostPath, emptyDir
- `hostPath`: 호스트와 볼륨을 공유하기 위해 사용
- `emptyDir`: 포드의 컨테이너 간에 볼륨을 공유하기 위해 사용

위 두가지는 자주 사용되는 볼륨 종류는 아니지만, 간단히 사용해 볼 수 있다.

### 워커 노드의 로컬 디렉토리를 볼륨으로 사용: hostPath
포드의 데이터를 보존할 수 있는 가장 간단한 방법은 호스트의 디렉토리를 포드와 공유해 데이터를 저장하는 것이다.

```yaml
# hostpath-pod.yaml
apiVersion: v1
kind: Pod
metadata:
  name: hostpath-pod
spec:
  containers:
    - name: my-container
      image: busybox
      args: [ "tail", "-f", "/dev/null" ]
      volumeMounts:
      - name: my-hostpath-volume
        mountPath: /etc/data
  volumes:
    - name: my-hostpath-volume
      hostPath:
        path: /tmp
```

위 예시에선 볼륨에서 `hostPath` 항목을 정의함으로써 호스트의 `/tmp`를 포드의 `/etc/data`에 마운트했다. 하지만, 이러한 방식의 데이터 보존은 바람직하지 않다. **디플로이먼트의 포드에 장애가 생겨 다른 노드로 포트가 옮겨갔을 경우, 이전 노드에 저장된 데이터를 사용할 수 없기 때문이다.**

이 방법은 특수한 경우를 제외하고 사용하지 않도록 하자.

### 포드 내의 컨테이너 간 임시 데이터 공유: emptyDir
`emptyDir` 볼륨은 포드의 데이터를 영속적으로 보존하기 위해 외부 볼륨을 사용하는 것이 아닌, **포드가 실행되는 도중에만 필요한 휘발성 데이터를 각 컨테이너가 함께 사용할 수 있도록 임시 저장 공간을 생성한다.** `emptyDir` 디렉토리는 비어있는 상태로 생성되며 포드가 삭제되면 여기에 저장된 데이터도 함께 삭제된다.

아파치 웹 서버를 실행하는 포드를 예시로 생성해보자.

```yaml
# emptydir-pod.yaml
apiVersion: v1
kind: Pod
metadata:
  name: emptydir-pod
spec:
  containers:
    - name: content-creator
      image: alicek106/alpine-wget:latest
      args: [ "tail", "-f", "/dev/null" ]
      volumeMounts:
      - name: my-emptydir-volume
        mountPath: /data # 이 컨테이너가 /data에 파일을 생성하면
  
    - name: apache-webserver
      image: httpd:2
      volumeMounts:
      - name: my-emptydir-volume
        mountPath: /usr/local/apache2/htdocs/ # 아파치 웹 서버에서 접근할 수 있다.

  volumes:
    - name: my-emptydir-volume
      emptyDir: {} # 포드 내에서 파일을 공유하는 emptyDir
```

`emptyDir`은 한 컨테이너가 파일을 관리하고 한 컨테이너가 그 파일을 사용하는 경우에 유용하게 사용할 수 있다. `content-creator` 컨테이너 내부로 들어가 `/data` 디렉토리에 웹 콘텐츠를 생성하면 아파치 웹 서버 컨테이너의 `htdocs` 디렉토리에도 동일하게 웹 콘텐츠 파일이 생성될 것이고, 이는 최종적으로 웹 서버에 의해 외부로 제공될 것이다.

```bash
$ kubectl apply -f emptydir-pod.yaml
pod/emptydir-pod created

$ kubectl exec -it emptydir-pod -c content-creator sh /
  # echo Hello, Kubernetes! >> /data/test.html

$ kubectl describe pod emptydir-pod | grep IP
Annotations:        cni.projectcalico.org/podIP: 172.17.0.8
IP:                 172.17.0.8

$ kubectl run -i --tty --rm debug \
    --image=alice106/ubuntu:curl --restart=Never -- curl 172.17.0.8/test.html
Hello, Kubernetes!
```

<img width="341" alt="image" src="https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/deafcbe1-e0e5-48d6-b85b-a1dfa0f78153">

## 네트워크 볼륨
<img width="590" alt="image" src="https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/b2c014e6-f1d3-43fb-a495-834bdc97b797">

쿠버네티스에선 별도의 플러그인을 설치하지 않아도 다양한 종류의 네트워크 볼륨을 포드에 마운트할 수 있다. 네트워크 볼륨의 위치는 특별히 정해진 것이 없으며, 네트워크로 접근할 수만 있으면 쿠버네티스 클러스터 내부, 외부 어느 곳에 존재해도 크게 상관은 없다. 단, AWS EBS와 같은 클라우드에 종속적인 볼륨을 사용하려면 AWS에서 쿠버네티스 클러스터를 생성할 때 특정 클라우드를 위한 옵션이 별도로 설정돼 있어야 한다.

NFS 볼륨의 사용법만 간단하게 정리해보자.

### NFS를 네트워크 볼륨으로 사용하기
**NFS(Network File System)는 대부분의 OS에서 사용할 수 있는 네트워크 스토리지로, 여러 개의 클라이언트가 동시에 마운트해 사용할 수 있다는 특징**이 있다. NFS는 여러 개의 스토리지를 클러스터링하는 다른 솔루션에 비해 안정성이 떨어질 순 있으나, 하나의 서버만으로 간편하게 사용할 수 있으며, NFS를 마치 로컬 스토리지처럼 사용할 수 있다는 장점이 있다.

NFS를 사용하려면 NFS 서버와 NFS 클라이언트가 각각 필요하다.
- NFS 서버: 영속적인 데이터가 실제로 저장되는 네트워크 스토리지 서버
- NFS 클라이언트: NFS 서버에 마운트해 스토리지에 파일을 읽고 쓰는 역할

NFS 클라이언트는 워커 노드의 기능을 사용하는 방식으로 예제를 살펴보자.

```yaml
# nfs-deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: nfs-server
spec:
  selector:
    matchLabels:
      role: nfs-server
  template:
    metadata:
      labels:
        role: nfs-server
    spec:
      containers:
      - name: nfs-server
        image: gcr.io/google_containers/volume-nfs:0.8
        ports:
          - name: nfs
            containerPort: 2049
          - name: mountd
            containerPort: 20048
          - name: rpcbind
            containerPort: 111
        securityContext:
          privileged: true
```

```yaml
# nfs-service.yaml
apiVersion: v1
kind: Service
metadata:
  name: nfs-service
spec:
  ports:
  - name: nfs
    port: 2049
  - name: mountd
    port: 20048
  - name: rpcbind
    port: 111
  selector:
    role: nfs-server
```

NFS 서버를 위한 디플로이먼트와 서비스를 위와 같이 생성한 뒤, 해당 NFS 서버의 볼륨을 포드에서 마운트해 데이터를 영속적으로 저장해보자.

```yaml
# nfs-pod.yaml
apiVersion: v1
kind: Pod
metadata:
  name: nfs-pod
spec:
  containers:
    - name: nfs-mount-container
      image: busybox
      args: [ "tail", "-f", "/dev/null" ]
      volumeMounts:
      - name: nfs-volume
        mountPath: /mnt # 포드 컨테이너 내부의 /mnt 디렉토리에 마운트한다.
  volumes:
    - name: nfs-volume
      nfs:  # NFS 서버의 볼륨을 포드의 컨테이너에 마운트한다.
        path: /
        server: {NFS_SERVICE_IP}
```

`mountPath`를 `/mnt`로 설정했기 때문에 NFS 서버의 네트워크 볼륨은 포드 컨테이너의 `/mnt` 디렉토리에 마운트될 것이다. 또한, `volumes` 항목에서 `nfs`라는 항목을 정의함으로써 NFS 서버의 볼륨을 사용한다고 명시했다.  
**위의 파일에서 유의할 부분은 `server` 항목이 `nfs-service`라는 서비스의 DNS 이름이 아닌 `{NFS_SERVICE_IP}`로 설정돼 있다는 것이다.** NFS 볼륨의 마운트는 컨테이너 내부가 아닌 워커 노드에서 발생하므로 서비스의 DNS 이름으로 NFS 서버에 접근할 수 없다. 노드에선 포드의 IP로 통신은 할 순 있지만, 쿠버네티스의 DNS를 사용하도록 설정돼 있진 않기 때문이다.

예외적으로 NFS 서비스의 `ClusterIP`를 직접 얻은 뒤, yaml 파일에 사용하는 방식으로 포드를 생성해보자.

```bash
# NFS 서버에 접근하기 위한 서비스의 Cluster IP를 얻는다.
$ export NFS_CLUSTER_IP=$(kubectl get svc/nfs-service -o jsonpath='{.spec.clusterIP}')

# nfs-pod의 server 항목을 NFS_CLUSTER_IP로 교채해 생성한다.
$ cat nfs-pod.yaml | sed "s/{NFS_SERVICE_IP}/$NFS_CLUSTER_IP/g" | kubectl apply -f -
```

> 📌 `kubectl get -o jsonpath`  
> 리소스의 특정 정보만 가져올 수 있는 옵션.

**실제 운영 환경에서 NFS 서버를 도입하려면 백업 스토리지를 별도로 구축해 NFS의 데이터 손실에 대비하거나, NFS 서버의 설정 튜닝 및 NFS 서버에 접근하기 위한 DNS 이름을 준비해야 할수도 있다.**

## PV, PVC를 이용한 볼륨 관리
지금까지 다뤘던 방식처럼 네트워크 볼륨이 파일에 고정적으로 명시됐을 경우, 디플로이먼트에 대해 yaml 파일을 다른 개발 부서에 배포하거나 웹상에 공개해야 한다면 네트워크 볼륨 타입을 명시하는 별도의 yaml 파일을 여러 개 만들어 배포해야 한다. **즉, 볼륨과 애플리케이션의 정의가 서로 밀접하게 연관돼 있어 서로 분리할 수 없는 상황이 돼버린다.**

**이러한 불편함을 해결하기 위해 쿠버네티스에선 퍼시스턴트 볼륨(Persistent Volume, PV)과 퍼시스턴트 볼륨 클레임(Persistent Volume Claim, PVC)이라는 오브젝트를 제공한다.** 이 두 개의 오브젝트는 **포드가 볼륨의 세부적인 사항을 몰라도 볼륨을 사용할 수 있도록 추상화해주는 역할을 담당한다.** 즉, 포드를 생성하는 yaml 입장에선 네트워크 볼륨이 NFS인지, AWS EBS인지 상관없이 볼륨을 사용할 수 있도록 하는 것이 핵심 아이디어다.

<img width="665" alt="image" src="https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/90660fa6-bc77-4966-98aa-c8d40bdb4f7b">

<img width="567" alt="image" src="https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/1ff28854-90e2-4074-8e59-cc18f7b140e4">

디플로이먼트를 생성하는 yaml 파일에서 `nfs` 항목을 정의하는 대신, `persistentVolumeClaim` 항목을 사용해 볼륨의 사용 여부만 나타내면 된다.

### PV와 PVC 사용하기
