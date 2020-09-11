import axios from 'axios'
import qs from 'query-string'


const service = axios.create({
  baseURL: 'http://localhost:8088',
  withCredentials: true, // 允许跨域 cookie
  headers: {
    'X-Requested-With': 'x-requested-with',
    'X-System-ID': 1, // 系统ID
  },
  xsrfCookieName: '_xsrf',
  xsrfHeaderName: 'X-Xsrftoken',
  transformRequest: [(data) => {
    if (data instanceof FormData) {
      return data
    }
    data = qs.stringify(data)
    return data
  }],
  transformResponse: [(data) => {
    let json = {}
    try {
      json = JSON.parse(data)
    } catch (e) {
      json = {}
    }
    return json
  }],
})

//请求拦截器
service.interceptors.request.use(
  config => {
    //请求之前做一些工作，比如带上token
    console.log("请求之前做一些工作,比如带上某些参数")
    return config
  },
  error => {
    //请求错误时做些事
    return Promise.reject(error)
  })

//返回拦截器
service.interceptors.response.use(
  response => {
    let result = response.data
    console.log("返回之后做一些工作,比如格式化数据")
    return result
  },
  error => {
    return Promise.reject(error)
  }
)

// get
export const doGet = (url, params) => {
  return service.get(`${url}`, {
    params: params
  })
}

// put
export const doPut = (url, data) => {
  return service({
    method: 'put',
    url: `${url}`,
    data: data,
  })
}

// post
export const doPost = (url, data) => {
  return service({
    method: 'post',
    url: `${url}`,
    data: data,
  })
}

// delete
export const doDelete = (url, data) => {
  return service({
    method: 'delete',
    url: `${url}`,
    data: data
  })
}

// upload
export const doUpload = (url, data) => {
  return service({
    method: 'post',
    headers: {'Content-Type': 'multipart/form-data'},
    url: `${url}`,
    data: data,
  })
}

//dowload
export const doDowload = (url) => {
  return service({
    url: `${url}`,
    method: 'get',
    responseType:'blob',
    headers: {'content-type':'application/x-download;charset=utf-8',},

  })
}



