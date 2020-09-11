import Vue from 'vue'
import Router from 'vue-router'
import Index from '@/components/Index'
import DataMethod from '@/components/DataMethod'
import ComputedAttribute from '@/components/ComputedAttribute'
import ConditionalRendering from '@/components/ConditionalRendering'
import ListRendering from '@/components/ListRendering'
import Child from '@/components/Child'
import Parent from '@/components/Parent'
import ContentSlot from '@/components/ContentSlot'
import ContentStore from '@/components/ContentStore'


Vue.use(Router)

export default new Router({
  routes: [
    {
      path: '/',
      name: 'Index',
      component: Index
    }, {
      path: '/DataMethod',
      name: 'DataMethod',
      component: DataMethod
    }, {
      path: '/ComputedAttribute',
      name: 'ComputedAttribute',
      component: ComputedAttribute
    }, {
      path: '/ConditionalRendering',
      name: 'ConditionalRendering',
      component: ConditionalRendering
    }, {
      path: '/ListRendering',
      name: 'ListRendering',
      component: ListRendering
    }, {
      path: '/Child',
      name: 'Child',
      component: Child
    }, {
      path: '/Parent',
      name: 'Parent',
      component: Parent
    }, {
      path: '/ContentSlot',
      name: 'ContentSlot',
      component: ContentSlot
    },{
      path: '/ContentStore',
      name: 'ContentStore',
      component: ContentStore
    }
  ]
})
