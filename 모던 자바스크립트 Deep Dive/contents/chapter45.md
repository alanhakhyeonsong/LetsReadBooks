# 45장. 프로미스
JavaScript는 비동기 처리를 위한 하나의 패턴으로 **콜백 함수**를 사용한다. 하지만 전통적인 콜백 패턴은 콜백 헬로 인해 가독성이 나쁘고 비동기 처리 중 발생한 에러의 처리가 곤란하며 여러 개의 비동기 처리를 한 번에 처리하는 데도 한계가 있다.

ES6에서는 비동기 처리를 위한 또 다른 패턴으로 `Promise`를 도입했다. **프로미스는 전통적인 콜백 패턴이 가진 단점을 보완하며 비동기 처리 시점을 명확하게 표현할 수 있다는 장점이 있다.

## 비동기 처리를 위한 콜백 패턴의 단점
### 콜백 헬
```javascript
// GET 요청을 위한 비동기 함수
const get = url => {
  const xhr = new XMLHttpRequest();
  xhr.open('GET', url);
  xhr.send();

  xhr.onload = () => {
    if (xhr.status === 200) {
      // 서버의 응답을 콘솔에 출력한다.
      console.log(JSON.parse(xhr.response));
    } else {
      console.error(`${xhr.status} ${xhr.statusText}`);
    }
  };
};

// id가 1인 post를 취득
get('https://jsonplaceholder.typicode.com/posts/1');
/*
{
  "userId": 1,
  "id": 1,
  "title": "sunt aut facere repellat provident occaecati excepturi optio reprehenderit",
  "body": "quia et suscipit\nsuscipit recusandae consequuntur expedita et cum\nreprehenderit molestiae ut ut quas totam\nnostrum rerum est autem sunt rem eveniet architecto"
}
*/
```

`get` 함수는 비동기 함수다.

비동기 함수란 **함수 내부에 비동기로 동작하는 코드를 포함한 함수를 말한다.**

- 비동기 함수 호출 시 함수 내부의 비동기로 동작하는 코드가 완료되지 않았다 해도 기다리지 않고 즉시 종료된다.
- 비동기 함수 내부의 비동기로 동작하는 코드는 비동기 함수가 종료된 이후에 완료된다.
- 비동기 함수 내부의 비동기로 동작하는 코드에서 처리 결과를 외부로 반환하거나 상위 스코프의 변수에 할당하면 기대한 대로 동작하지 않는다.

```javascript
let g = 0;

// 비동기 함수인 setTimeout 함수는 콜백 함수의 처리 결과를 외부로 반환하거나 상위 스코프의 변수에 할당하지 못한다.
setTimeout(() => { g = 100; }, 0);
console.log(g); // 0
```

`get` 함수가 비동기 함수인 이유는 `get` 함수 내부의 `onload` 이벤트 핸들러가 비동기로 동작하기 때문이다.

- `get` 함수를 호출하면 GET 요청을 전송하고 `onload` 이벤트 핸들러를 등록한 다음 `undefined`를 반환하고 즉시 종료된다.
- `get` 함수 내부의 `onload` 이벤트 핸들러는 `get` 함수가 종료된 이후에 실행된다.
- `get` 함수의 `onload` 이벤트 핸들러에서 서버의 응답 결과를 반환하거나 상위 스코프의 변수에 할당하면 기대한 대로 동작하지 않는다.

```javascript
// GET 요청을 위한 비동기 함수
const get = url => {
  const xhr = new XMLHttpRequest();
  xhr.open('GET', url);
  xhr.send();

  xhr.onload = () => {
    if (xhr.status === 200) {
      // 1. 서버의 응답을 반환한다. (get 함수의 반환문이 아니다)
      return JSON.parse(xhr.response);
    } else {
      console.error(`${xhr.status} ${xhr.statusText}`);
    }
  };
};

// 2. id가 1인 post를 취득
const response = get('https://jsonplaceholder.typicode.com/posts/1');
console.log(response); // undefined
```

`get` 함수 호출 → `XMLHttpRequest` 객체 생성 → HTTP 요청 초기화 → HTTP 요청 전송 → `xhr.onload` 이벤트 핸들러 프로퍼티에 이벤트 핸들러를 바인딩하고 종료.

이때 `get` 함수에 명시적인 반환문이 없으므로 `undefined`를 반환한다.

함수의 반환값은 명시적으로 호출한 다음에 캐치할 수 있으므로 `onload` 이벤트 핸들러를 `get` 함수가 호출할 수 있다면 이벤트 핸들러의 반환값을 `get` 함수가 캐치하여 다시 반환할 수도 있겠지만 `onload` 이벤트 핸들러는 `get` 함수가 호출하지 않기 때문에 그럴 수도 없다. 따라서 `onload` 이벤트 핸들러의 반환값은 캐치할 수 없다.

```html
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Document</title>
</head>
<body>
  <input type="text">
  <script>
    document.querySelector('input').oninput = function () {
      console.log(this.value);
      // 이벤트 핸들러에서의 반환은 의미가 없다.
      return this.value;
    };
  </script>
</body>
</html>
```

서버의 응답을 상위 스코프의 변수에 할당해도 역시나 기대한 대로 동작하지 않는다.

```javascript
let todos;

// GET 요청을 위한 비동기 함수
const get = url => {
  const xhr = new XMLHttpRequest();
  xhr.open('GET', url);
  xhr.send();

  xhr.onload = () => {
    if (xhr.status === 200) {
      // 1. 서버의 응답을 상위 스코프의 변수에 할당한다.
      todos = JSON.parse(xhr.response);
    } else {
      console.error(`${xhr.status} ${xhr.statusText}`);
    }
  };
};

// id가 1인 post를 취득
get('https://jsonplaceholder.typicode.com/posts/1');
console.log(todos); // 2. undefined
```

`xhr.onload` 이벤트 핸들러 프로퍼티에 바인딩한 이벤트 핸들러는 언제나 `console.log`가 종료한 이후에 호출된다. 따라서 2의 시점에서는 아직 전역 변수 `todos`에 서버의 응답 결과가 할당되기 이전이다.

비동기 함수 `get` 호출되면 함수 코드를 평가하는 과정에서 `get` 함수의 실행 컨텍스트가 생성되고 실행 컨텍스트 스택(콜 스택)에 푸시된다. 이후 함수 코드 실행 과정에서 `xhr.onload` 이벤트 핸들러 프로퍼티에 이벤트 핸들러가 바인딩 된다.

`get` 함수가 종료하면 `get` 함수의 실행 컨텍스트가 콜 스택에서 팝되고, 곧바로 2의 `console.log`가 호출된다. 이때 `console.log`의 실행 컨텍스트가 생성되어 실행 컨텍스트 스택에 푸시된다. 만약 `console.log`가 호출되기 직전에 `load` 이벤트가 발생했더라도 `xhr.onload` 이벤트 핸들러 프로퍼티에 바인딩한 이벤트 핸들러는 결코 `console.log`보다 먼저 실행되지 않는다.

서버로부터 응답이 도착하면 `xhr` 객체에서 `load` 이벤트가 발생한다. 이때 **`xhr.onLoad` 핸들러 프로퍼티에 바인딩한 이벤트 핸들러가 즉시 실행되는 것이 아니다. `xhr.onLoad` 이벤트 핸들러는 `load` 이벤트가 발생하면 일단 태스크 큐 에 저장되어 대기하다가, 콜 스택이 비면 이벤트 루프에 의해 콜 스택으로 푸시되어 실행된다.** 이벤트 핸들러도 함수이므로 `이벤트 핸들러의 평가 → 이벤트 핸들러의 실행 컨텍스트 생성 → 콜 스택에 푸시 → 이벤트 핸들러 실행` 과정을 거친다.

**이처럼 비동기 함수는 비동기 처리 결과를 외부에 반환할 수 없고, 상위 스코프의 변수에 할당할 수도 없다. 따라서 비동기 함수의 처리 결과(서버의 응답 등)에 대한 후속 처리는 비동기 함수 내부에서 수행해야 한다. 이때 비동기 함수를 범용적으로 사용하기 위해 비동기 함수에 비동기 처리 결과에 대한 후속 처리를 수행하는 콜백 함수를 전달하는 것이 일반적이다. 필요에 따라 비동기 처리가 성공하면 호출될 콜백 함수와 비동기 처리가 실패하면 호출될 콜백 함수를 전달할 수 있다.**

```javascript
// GET 요청을 위한 비동기 함수
const get = (url, successCallback, failureCallback) => {
  const xhr = new XMLHttpRequest();
  xhr.open('GET', url);
  xhr.send();

  xhr.onload = () => {
    if (xhr.status === 200) {
      // 서버의 응답을 콜백 함수에 인수로 전달하면서 호출하여 응답에 대한 후속 처리를 한다.
      successCallback(JSON.parse(xhr.response));
    } else {
      // 에러 정보를 콜백 함수에 인수로 전달하면서 호출하여 에러 처리를 한다.
      failureCallback(xhr.status);
    }
  };
};

// id가 1인 post를 취득
// 서버의 응답에 대한 후속 처리르 위한 콜백 함수를 비동기 함수인 get에 전달해야 한다.
get('https://jsonplaceholder.typicode.com/posts/1', console.log, console.error);
/*
{
  "userId": 1,
  "id": 1,
  "title": "sunt aut facere repellat provident occaecati excepturi optio reprehenderit",
  "body": "quia et suscipit\nsuscipit recusandae consequuntur expedita et cum\nreprehenderit molestiae ut ut quas totam\nnostrum rerum est autem sunt rem eveniet architecto"
}
*/
```

이처럼 콜백 함수를 통해 비동기 처리 결과에 대한 후속 처리를 수행하는 비동기 함수가 비동기 처리 결과를 가지고 또다시 비동기 함수를 호출해야 한다면 콜백 함수 호출이 중첩되어 복잡도가 높아지는 현상이 발생하는데, 이를 **콜백 헬**이라 한다.

```javascript
get('/step1', a => {
  get(`/step2/${a}`, b => {
    get(`/step3/${b}`, c => {
      get(`/step4/${c}`, d => {
        console.log(d);
      })
    })
  })
});
```

### 에러 처리의 한계
비동기 처리를 위한 콜백 패턴의 문제점 중 가장 심각한 것은 에러 처리가 곤란하다는 것이다.

```javascript
try {
  setTimeout(() => { throw new Error('Error!'); }, 1000);
} catch (e) {
  // 에러를 캐치하지 못한다.
  console.error('캐치한 에러', e);
}
```

`try` 코드 블록 내에서 호출한 `setTimeout` 함수는 1초 후에 콜백 함수가 실행되도록 타이머를 설정하고, 이후 콜백 함수는 에러를 발생시킨다. 하지만 이 에러는 `catch` 코드 블록에서 캐치되지 않는다.

비동기 함수인 `setTimeout`이 호출되면 `setTimeout` 함수의 실행 컨텍스트가 생성되어 콜 스택에 푸시되어 실행된다. `setTimeout`은 비동기 함수이므로 콜백 함수가 호출되는 것을 기다리지 않고 즉시 종료되어 콜 스택에서 제거된다. 이후 타이머가 만료되면 `setTimeout` 함수의 콜백 함수는 태스크 큐로 푸시되고 콜 스택이 비어졌을 때 이벤트 루프에 의해 콜 스택으로 푸시되어 실행된다.

`setTimeout` 함수의 콜백 함수가 실행될 때 `setTimeout` 함수는 이미 콜 스택에서 제거된 상태다. 이것은 `setTimeout` 함수의 콜백 함수를 호출한 것이 `setTimeout` 함수가 아니라는 것을 의미한다. `setTimeout` 함수의 콜백 함수의 호출자가 `setTimeout` 함수라면 콜 스택의 현재 실행 중인 실행 컨텍스트가 콜백 함수의 실행 컨텍스트일 때 현재 실행 중인 실행 컨텍스트의 하위 실행 컨텍스트가 `setTimeout` 함수여야 한다.

**에러는 호출자 방향으로 전파된다.** 즉, 콜 스택 아래 방향으로 전파된다. (실행 중인 실행 컨텍스트가 푸시되기 직전에 푸시된 실행 컨텍스트 방향으로 전파)

하지만 `setTimeout` 함수의 콜백 함수를 호출한 것은 `setTimeout` 함수가 아니다. 따라서 이 함수의 콜백 함수가 발생시킨 에러는 `catch` 블록에서 캐치되지 않는다.

비동기 처리를 위한 콜백 패턴은 콜백 헬이나 에러 처리가 곤란하다는 문제가 있다. 이를 극복하기 위해 ES6에서 프로미스가 도입되었다.

## 프로미스의 생성
`Promise` 생성자 함수를 `new` 연산자와 함께 호출하면 프로미스 객체를 생성한다. ES6에서 도입된 `Promise`는 호스트 객체가 아닌 ECMAScript 사양에 정의된 표준 빌트인 객체다.

`Promise` 생성자 함수는 비동기 처리를 수행할 콜백 함수(`executor`)를 인수로 전달받는데 이 콜백 함수는 `resolve`, `reject` 함수를 인수로 전달받는다.

```javascript
// 프로미스 생성
const promise = new Promise((resolve, reject) => {
  // Promise 함수의 콜백 함수 내부에서 비동기 처리를 수행한다.
  if (/* 비동기 처리 성공 */) {
    resolve('result');
  } else { // 비동기 처리 실패
    reject('failure reason');
  }
});
```

```javascript
// GET 요청을 위한 비동기 함수
const promiseGet = url => {
  return new Promise((resolve, reject) => {
    const xhr = new XMLHttpRequest();
    xhr.open('GET', url);
    xhr.send();

    xhr.onload = () => {
      if (xhr.status === 200) {
        // 성공적으로 응답을 전달받으면 resolve 함수를 호출한다.
        resolve(JSON.parse(xhr.response));
      } else {
        // 에러 처리를 위해 reject 함수를 호출
        reject(new Error(xhr.status));
      }
    };
  });
};

// promiseGet 함수는 프로미스를 반환한다.
promiseGet('https://jsonplaceholder.typicode.com/posts/1');
```

프로미스는 다음과 같이 현재 비동기 처리가 어떻게 진행되고 있는지 나타내는 상태 정보를 갖는다.

|프로미스의 상태 정보|의미|상태 변경 조건|
|--|--|--|
|`pending`|비동기 처리가 |프로미스가 생성된 직후 기본상태|
|`fulfilled`|비동기 처리가 수행된 상태(성공)|`resolve` 함수 호출|
|`rejected`|비동기 처리가 수행된 상태(실패)|`reject` 함수 호출|

생성된 직후의 프로미스는 기본적으로 `pending` 상태다. 이후 비동기 처리가 수행되면 비동기 처리 결과에 따라 다음과 같이 프로미스의 상태가 변경된다.

- 비동기 처리 성공: `resolve` 함수를 호출해 프로미스를 `fulfilled` 상태로 변경한다.
- 비동기 처리 실패: `reject` 함수를 호출해 프로미스를 `rejected` 상태로 변경한다.

<img width="385" alt="image" src="https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/2630ea41-2e46-4025-afe0-3974fb3e6045">

`fulfilled` 또는 `rejected` 상태를 `settled` 상태라고 한다. 이는 둘 중 어떤 상태와 상관없이 `pending`이 아닌 상태로 비동기 처리가 수행된 상태를 말한다. 일단 `settled` 상태가 되면 더는 다른 상태로 변화할 수 없다.

```javascript
// fulfilled된 프로미스
const fulfilled = new Promise(resolve => resolve(1));

// rejected된 프로미스
const rejected = new Promise((_, reject) => reject(new Error('error occurred')));
```

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/123f0b9c-6c11-4421-a851-b08b6089f3bc)

- 비동기 처리가 성공하면 프로미스는 `pending` 상태에서 `fulfilled` 상태로 변화한다. 그리고 비동기 처리 결과인 1을 값으로 갖는다.
- 비동기 처리가 실패하면 프로미스는 `pending` 상태에서 `rejected` 상태로 변화한다. 그리고 비동기 처리 결과인 `Error` 객체를 값으로 갖는다.

즉, **프로미스는 비동기 처리 상태와 처리 결과를 관리하는 객체다.**

## 프로미스의 후속 처리 메서드
프로미스의 비동기 처리 상태가 변화하면 이에 따른 후속 처리를 해야 한다. 이를 위해 프로미스는 후속 메서드 `then`, `catch`, `finally`를 제공한다.

**프로미스의 비동기 처리 상태가 변화하면 후속 처리 메서드에 인수로 전달한 콜백 함수가 선택적으로 호출된다.** 이때 후속 처리 메서드의 콜백 함수에 프로미스의 처리 결과가 인수로 전달된다. 모든 후속 처리 메서드는 프로미스를 반환하며, 비동기로 동작한다.

### Promise.prototype.then
`then` 메서드는 두 개의 콜백 함수를 인수로 전달받는다.

- 첫 번째 콜백 함수는 프로미스가 `fulfilled` 상태(`resolve` 함수가 호출된 상태)가 되면 호출된다. 이때 콜백 함수는 프로미스의 비동기 처리 결과를 인수로 전달받는다.
- 두 번째 콜백 함수는 프로미스가 `fulfilled` 상태(`reject` 함수가 호출된 상태)가 되면 호출된다. 이때 콜백 함수는 프로미스의 에러를 인수로 전달받는다.

```javascript
// fulfilled
new Promise(resolve => resolve('fulfilled'))
  .then(v => console.log(v), e => console.error(e)); // fulfilled

// rejected
new Promise((_, reject) => reject(new Error('rejected')))
  .then(v => console.log(v), e => console.error(e)); // Error: rejected
```

`then` 메서드는 언제나 프로미스를 반환한다. 만약 `then` 메서드의 콜백 함수가 프로미스를 반환하면 그 프로미스를 그대로 반환하고, 콜백 함수가 프로미스가 아닌 값을 반환하면 그 값을 암묵적으로 `resolve` 또는 `reject` 하여 프로미스를 생성해 반환한다.

### Promise.prototype.catch
`catch` 메서드는 한 개의 콜백 함수를 인수로 전달받는다. `catch` 메서드의 콜백 함수는 프로미스가 `rejected` 상태인 경우에만 호출된다.

```javascript
// rejected
new Promise((_, reject) => reject(new Error('rejected')))
  .catch(e => console.error(e)); // Error: rejected
```

`catch` 메서드는 `then(undefined, onRejected)`과 동일하게 동작한다. 따라서 언제나 프로미스를 반환한다.

### Promise.prototype.finally
`finally` 메서드는 한 개의 콜백 함수를 인수로 전달받는다. `finally` 메서드의 콜백 함수는 프로미스의 성공(`fulfilled`) 또는 실패(`rejected`)와 상관없이 무조건 한 번 호출된다. `finally` 메서드는 프로미스의 상태와 상관없이 공통적으로 수행해야 할 처리 내용이 있을 때 유용하다. `finally` 메서드도 `then/catch` 메서드와 마찬가지로 언제나 프로미스를 반환한다.

```javascript
new Promise(() => {})
  .finally(() => console.log('finally')); // finally
```

```javascript
// GET 요청을 위한 비동기 함수
const promiseGet = url => {
  return new Promise((resolve, reject) => {
    const xhr = new XMLHttpRequest();
    xhr.open('GET', url);
    xhr.send();

    xhr.onload = () => {
      if (xhr.status === 200) {
        // 성공적으로 응답을 전달받으면 resolve 함수를 호출한다.
        resolve(JSON.parse(xhr.response));
      } else {
        // 에러 처리를 위해 reject 함수를 호출
        reject(new Error(xhr.status));
      }
    };
  });
};

// promiseGet 함수는 프로미스를 반환한다.
promiseGet('https://jsonplaceholder.typicode.com/posts/1')
  .then(res => console.log(res))
  .catch(err => console.error(err))
  .finally(() => console.log('Bye!'));
```

## 프로미스의 에러 처리
위 예제의 비동기 함수 `get`은 프로미스를 반환한다. 비동기 처리 결과에 대한 후속 처리는 프로미스가 제공하는 후속 처리 메서드 `then`, `catch`, `finally`를 사용하여 수행한다. 비동기 처리에서 발생한 에러는 `then` 메서드의 두 번째 콜백 함수로 처리할 수 있다.

```javascript
const wrongUrl = 'https://jsonplaceholder.typicode.com/XXX/1'

// 부적절한 URL이 지정되었기 때문에 에러가 발생한다.
promiseGet(wrongUrl).then(
  res => console.log(res),
  err => console.error(err)
); // Error: 404
```

비동기 처리에서 발생한 에러는 프로미스의 후속 처리 메서드 `catch`를 사용해 처리할 수도 있다.

```javascript
const wrongUrl = 'https://jsonplaceholder.typicode.com/XXX/1'

// 부적절한 URL이 지정되었기 때문에 에러가 발생한다.
promiseGet(wrongUrl)
  .then(res => console.log(res))
  .catch(err => console.error(err)); // Error: 404
```

`catch` 메서드를 호출하면 내부적으로 `then(undefined, onRejected)`을 호출한다. 따라서 내부적으로 다음과 같이 처리된다.

```javascript
const wrongUrl = 'https://jsonplaceholder.typicode.com/XXX/1'

// 부적절한 URL이 지정되었기 때문에 에러가 발생한다.
promiseGet(wrongUrl)
  .then(res => console.log(res))
  .then(undefined, err => console.error(err)); // Error: 404
```

단, `then` 메서드의 두 번째 콜백 함수는 첫 번째 콜백 함수에서 발생한 에러를 캐치하지 못하고 코드가 복잡해져서 가독성이 좋지 않다.

```javascript
promiseGet('https://jsonplaceholder.typicode.com/todos/1').then(
  res => console.log(res),
  err => console.error(err)
); // 두 번째 콜백 함수는 첫번째 콜백 함수에서 발생한 에러를 캐치하지 못한다. 
```

`catch` 메서드를 모든 `then` 메서드를 호출한 이후에 호출하면 비동기 처리에서 발생한 에러뿐만 아니라 `then` 메서드 내부에서 발생한 에러까지 모두 캐치할 수 있다.

```javascript
promiseGet('https://jsonplaceholder.typicode.com/todos/1')
  .then(res => console.xxx(res))
  .catch(err => console.error(err)); // TypeError: console.xxx is not a function
```

또한 `then` 메서드에 두 번째 콜백 함수를 전달하는 것보다 `catch` 메서드를 사용하는 것이 가독성이 좋고 명확하다. 따라서 에러 처리는 `then` 메서드에서 하지 말고 `catch` 메서드에서 하는 것을 권장한다.

## 프로미스 체이닝
```javascript
const url = 'https://jsonplaceholder.typicode.com';

// id가 1인 post의 userId를 취득
promiseGet(`${url}/posts/1`)
  // 취득한 post의 userId로 user 정보를 취득
  .then(({ userId }) => promiseGet(`${url}/users/${userId}`))
  .then(userInfo => console.log(userInfo))
  .catch(err => console.error(err));
```

**`then`, `catch`, `finally` 후속 처리 메서드는 언제나 프로미스를 반환하므로 연속적으로 호출할 수 있다.** 이를 프로미스 체이닝이라 한다.

후속 처리 메서드의 콜백 함수는 프로미스의 비동기 처리 상태가 변경되면 선택적으로 호출된다. 위 예제에서 후속 처리 메서드의 콜백 함수는 다음과 같이 인수를 전달받으면서 호출된다.

![image](https://github.com/alanhakhyeonsong/LetsReadBooks/assets/60968342/3ee73b92-ba31-415a-9194-c8391a8f1950)

이처럼 `then`, `catch`, `finally` 후속 처리 메서드는 콜백 함수가 반환한 프로미스를 반환한다. 만약 후속 처리 메서드의 콜백 함수가 프로미스가 아닌 값을 반환하더라도 그 값을 암묵적으로 `resolve` 또는 `reject`하여 프로미스를 생성해 반환한다.

**프로미스는 프로미스 체이닝을 통해 비동기 처리 결과를 전달받아 후속 처리를 하므로 비동기 처리를 위한 콜백 패턴에서 발생하던 콜백 헬이 발생하지 않는다.** 다만 프로미스도 콜백 패턴을 사용하므로 콜백 함수를 사용하지 않는 것은 아니다.

콜백 패턴은 가독성이 좋지 않다. 이 문제는 ES8에서 도입된 `async/await`를 통해 해결할 수 있다. 이를 사용하면 프로미스의 후속 처리 메서드 없이 마치 동기 처리처럼 프로미스가 처리 결과를 반환하도록 구현할 수 있다.

```javascript
const url = 'https://jsonplaceholder.typicode.com';

(async () => {
  // id가 1인 post의 userId를 취득
  const { userId } = await promiseGet(`${url}/posts/1`);

  // 취득한 post의 userId로 user 정보를 취득
  const userInfo = await promiseGet(`${url}/users/${userId}`);

  console.log(userInfo);
})();
```

## 프로미스의 정적 메서드
`Promise`는 주로 생성자 함수로 사용되지만 함수도 객체이므로 메서드를 가질 수 있다.

### Promise.resolve / Promise.reject
`Promise.resolve`와 `Promise.reject` 메서드는 이미 존재하는 값을 래핑하여 프로미스를 생성하기 위해 사용한다.

`Promise.resolve`는 인수로 전달받은 값을 `resolve`하는 프로미스를 생성한다.

```javascript
// 배열을 resolve하는 프로미스를 생성
const resolvedPromise = Promise.resolve([1, 2, 3]);
resolvedPromise.then(console.log); // [1, 2, 3]

// 다음과 같이 동일하게 동작한다.
const resolvedPromise = new Promise(resolve => resolve([1, 2, 3]));
resolvedPromise.then(console.log); // [1, 2, 3]
```

`Promise.reject`는 인수로 전달받은 값을 `reject`하는 프로미스를 생성한다.

```javascript
// 에러 객체를 reject하는 프로미스를 생성
const rejectedPromise = Promise.reject(new Error('Error!'));
rejectedPromise.catch(console.log); // Error: Error!

// 다음과 같이 동일하게 동작한다.
const rejectedPromise = new Promise((_, reject) => reject(new Error('Error!')));
rejectedPromise.catch(console.log); // Error: Error!
```

### Promise.all
`Promise.all` 메서드는 여러 개의 비동기 처리를 모두 병렬 처리할 때 사용한다.

```javascript
const requestData1 = () => new Promise(resolve => setTimeout(() => resolve(1), 3000));
const requestData2 = () => new Promise(resolve => setTimeout(() => resolve(2), 2000));
const requestData3 = () => new Promise(resolve => setTimeout(() => resolve(3), 1000));

// 세 개의 비동기 처리를 순차적으로 처리
const res = [];
requestData1()
  .then(data => {
    res.push(data);
    return requestData2();
  })
  .then(data => {
    res.push(data);
    return requestData3();
  })
  .then(data => {
    res.push(data);
    console.log(res); // [1, 2, 3] (약 6초 소요)
  })
  .catch(console.error);
```

위 예제는 세 개의 비동기 처리를 순차적으로 처리한다. 앞선 비동기 처리가 완료하면 다음 비동기 처리를 수행한다. 따라서 총 6초 이상이 소요된다.

그런데 위 예제의 경우 세 개의 비동기 처리는 서로 의존하지 않고 개별적으로 수행된다. 즉, 앞선 비동기 처리 결과를 다음 비동기 처리가 사용하지 않는다. 따라서 위 예제의 경우 세 개의 비동기 처리를 순차적으로 처리할 필요가 없다.

`Promise.all` 메서드는 여러 개의 비동기 처리를 모두 병렬 처리할 때 사용한다고 했다. 이를 사용해보자.

```javascript
const requestData1 = () => new Promise(resolve => setTimeout(() => resolve(1), 3000));
const requestData2 = () => new Promise(resolve => setTimeout(() => resolve(2), 2000));
const requestData3 = () => new Promise(resolve => setTimeout(() => resolve(3), 1000));

// 세 개의 비동기 처리를 병렬로 처리
Promise.all([requestData1(), requestData2(), requestData3()])
  .then(console.log) // [1, 2, 3] (약 3초 소요)
  .catch(console.error);
```

`Promise.all` 메서드는 프로미스를 요소로 갖는 배열 등의 이터러블을 인수로 전달 받는다. 그리고 전달받은 모든 프로미스가 모두 `fulfilled` 상태가 되면 모든 처리 결과를 배열에 저장해 새로운 프로미스를 반환한다.

`Promise.all` 메서드는 인수로 전달받은 배열의 모든 프로미스가 모두 `fulfilled` 상태가 되면 종료한다. 따라서 `Promise.all` 메서드가 종료하는 데 걸리는 시간은 가장 늦게 `fulfilled` 상태가 되는 프로미스의 처리 시간보다 조금 더 길다.

첫 번째 프로미스가 가장 나중에 `fulfilled` 상태가 되어도 `Promise.all` 메서드는 첫 번재 프로미스가 `resolve`한 처리 결과부터 차례대로 배열에 저장해 그 배열을 `resolve`하는 새로운 프로미스를 반환한다. 즉, 처리 순서가 보장된다.

`Promise.all` 메서드는 인수로 전달받은 배열의 프로미스가 하나라도 `rejected` 상태가 되면 나머지 프로미스가 `fulfilled` 상태가 되는 것을 기다리지 않고 즉시 종료한다.

```javascript
Promise.all([
  new Promise((_, reject) => setTimeout(() => reject(new Error('Error 1')), 3000)),
  new Promise((_, reject) => setTimeout(() => reject(new Error('Error 2')), 2000)),
  new Promise((_, reject) => setTimeout(() => reject(new Error('Error 3')), 1000))
])
  .then(console.log)
  .catch(console.log); // Error: Error 3
```

위 예제의 경우 세 번째 프로미스가 가장 먼저 `rejected` 상태가 되므로 세 번째 프로미스가 `reject`한 에러가 `catch` 메서드로 전달된다.

`Promise.all` 메서드는 인수로 전달받은 이터러블의 요소가 프로미스가 아닌 경우 `Promise.resolve` 메서드를 통해 프로미스로 래핑한다.

```javascript
Promise.all([
  1, // → Promise.resolve(1)
  2, // → Promise.resolve(2)
  3, // → Promise.resolve(3)
])
  .then(console.log) // [1, 2, 3]
  .catch(console.log);
```

### Promise.race
`Promise.race` 메서드는 `Promise.all` 메서드와 동일하게 프로미스를 요소로 갖는 배열 등의 이터러블을 인수로 전달받는다.

`Promise.race` 메서드는 `Promise.all` 메서드처럼 모든 프로미스가 `fulfilled` 상태가 되는 것을 기다리는 것이 아니라 가장 먼저 `fulfilled` 상태가 된 프로미스의 처리 결과를 `resolve`하는 새로운 프로미스를 반환한다.

```javascript
Promise.race([
  new Promise(resolve => setTimeout(() => resolve(1), 3000)), // 1
  new Promise(resolve => setTimeout(() => resolve(2), 2000)), // 2
  new Promise(resolve => setTimeout(() => resolve(3), 1000)) // 3
])
  .then(console.log) // 3
  .catch(console.log);
```

프로미스가 `rejected` 상태가 되면 `Promise.all` 메서드와 동일하게 처리된다. 즉, `Promise.race` 메서드에 전달된 프로미스가 하나라도 `rejected` 상태가 되면 에러를 `reject`하는 새로운 프로미스를 즉시 반환한다.

```javascript
Promise.race([
  new Promise((_, reject) => setTimeout(() => reject(new Error('Error 1')), 3000)),
  new Promise((_, reject) => setTimeout(() => reject(new Error('Error 2')), 2000)),
  new Promise((_, reject) => setTimeout(() => reject(new Error('Error 3')), 1000))
])
  .then(console.log)
  .catch(console.log); // Error: Error 3
```

### Promise.allSettled


```javascript
Promise.allSettled([
  new Promise(resolve => setTimeout(() => resolve(1), 2000)),
  new Promise((_, reject) => setTimeout(() => reject(2), 1000))
]).then(console.log);
/*
[
  {status: "fulfilled", value: 1},
  {status: "rejected", reason: Error: Error! at <anonymous>:3:54}
]
*/
```

`Promise.allSettled` 메서드가 반환한 배열에는 `fulfilled` 또는 `rejected` 상태와는 상관없이 `Promise.allSettled` 메서드가 인수로 전달받은 모든 프로미스들의 처리 결과가 모두 담겨 있다. 프로미스의 처리 결과를 나타내는 객체는 다음과 같다.

- 프로미스가 `fulfilled` 상태인 경우 비동기 처리 상태를 나타내는 `status` 프로퍼티와 처리 결과를 나타내는 `value` 프로퍼티를 갖는다.
- 프로미스가 `rejected` 상태인 경우 비동기 처리 상태를 나타내는 `status` 프로퍼티와 에러를 나타내는 `reason` 프로퍼티를 갖는다.

```javascript
[
  // 프로미스가 fulfilled 상태인 경우
  {status: "fulfilled", value: 1},
  // 프로미스가 rejected 상태인 경우
  {status: "rejected", reason: Error: Error! at <anonymous>:3:60}
]
```

## 마이크로태스크 큐
```javascript
setTimeout(() => console.log(1), 0);

Promise.resolve()
  .then(() => console.log(2))
  .then(() => console.log(3));
```

프로미스의 후속 처리 메서드도 비동기로 동작하므로 `1 → 2 → 3` 순으로 출력될 것으로 보이지만 `2 → 3 → 1` 순으로 출력된다. **그 이유는 프로미스의 후속 처리 메서드의 콜백 함수는 태스크 큐가 아니라 마이크로태스크 큐에 저장되기 때문이다.**

마이크로태스크 큐는 태스크 큐와는 별도의 큐다. 마이크로테스크 큐에는 프로미스의 후속 처리 메서드의 콜백 함수가 일시 저장된다. 그 외의 비동기 함수의 콜백 함수나 이벤크 핸들러는 태스크 큐에 일시 저장된다.

콜백 함수나 이벤트 핸들러를 일시 저장한다는 점에서 태스크 큐와 동일하지만 **마이크로태스크 큐는 태스크 큐보다 우선순위가 높다.** 즉, 이벤트 루프는 콜 스택이 비면 먼저 마이크로태스크 큐에서 대기하고 있는 함수를 가져와 실행한다. 이후 마이크로태스크 큐가 비면 태스크 큐에서 대기하고 있는 함수를 가져와 실행한다.