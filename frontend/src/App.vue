<script setup lang="ts">
import { computed, ref } from 'vue'

// 当前登录用户的数据结构。
// 这几个字段和后端 LoginResponse.UserInfo 返回的字段保持一致。
type CurrentUser = {
  id: number
  username: string
  nickname: string
  avatarUrl: string | null
  role: string
}

// 商品列表中每一行的数据结构。
// 这几个字段和后端 ProductListItemResponse 返回的字段保持一致。
type ProductItem = {
  id: number
  productCode: string
  productName: string
  category: string
  unit: string
  safeStock: number
  status: number
  mainImageUrl: string | null
  createdAt: string
}

// 商品详情里的图片数据结构。
// 字段和后端 ProductDetailResponse.ImageInfo 保持一致。
type ProductImage = {
  id: number
  imageUrl: string
  isMain: number
  sortOrder: number
}

// 商品详情数据结构。
// 它比列表项多了 images，因为详情页需要展示商品图片列表。
type ProductDetail = {
  id: number
  productCode: string
  productName: string
  category: string
  unit: string
  safeStock: number
  status: number
  images: ProductImage[]
}

// 表单输入框绑定的数据
const username = ref('')
const password = ref('')

// 登录页面状态
const loading = ref(false)
const errorMessage = ref('')
const currentUser = ref<CurrentUser | null>(null)

// 登录成功后的商品列表页面状态
const productLoading = ref(false)
const productError = ref('')
const productKeyword = ref('')
const products = ref<ProductItem[]>([])
const productTotal = ref(0)
const productPage = ref(1)
const productPageSize = ref(8)
const activeStatus = ref<number | null>(null)
const activeCategory = ref('')
const detailLoading = ref(false)
const detailError = ref('')
const selectedProduct = ref<ProductDetail | null>(null)

// 根据后端返回的 total 和当前 pageSize 计算总页数。
// 例如 total = 16，pageSize = 8，总页数就是 2。
// Math.max(1, ...) 是为了避免 total = 0 时页面显示“第 1 / 0 页”。
const productTotalPages = computed(() => {
  return Math.max(1, Math.ceil(productTotal.value / productPageSize.value))
})

// 当前页第一条数据的序号，用来显示 “Showing 1-8 of 16”。
const productStartIndex = computed(() => {
  if (productTotal.value === 0) {
    return 0
  }

  return (productPage.value - 1) * productPageSize.value + 1
})

// 当前页最后一条数据的序号。
// 最后一页可能不满 pageSize，所以要用 Math.min 防止超过 total。
const productEndIndex = computed(() => {
  return Math.min(productPage.value * productPageSize.value, productTotal.value)
})

// 左侧筛选项先用静态数据做界面。
// 后面你实现分类接口、仓库接口以后，可以把这些静态数组改成后端返回的数据。
const statusFilters = [
  { label: '全部商品', value: null },
  { label: '已启用', value: 1 },
  { label: '已停用', value: 0 },
]
const categoryFilters = ['全部分类', '咖啡豆', '咖啡饮品', '耗材', '杯具', '设备']
const libraryFilters = ['MySQL', 'Spring Boot', 'Vue', 'JWT', 'JdbcTemplate']

// 登录方法：点击 Login 按钮时执行
async function handleLogin() {
  errorMessage.value = ''

  if (!username.value || !password.value) {
    errorMessage.value = '请输入用户名和密码'
    return
  }

  loading.value = true

  try {
    const response = await fetch('/api/auth/login', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },

      // 请求体字段必须和后端 LoginRequest 对应：
      // username -> 用户名
      // password -> 密码
      body: JSON.stringify({
        username: username.value,
        password: password.value,
      }),
    })

    const result = await response.json()

    if (result.code !== 0) {
      errorMessage.value = result.message || '登录失败'
      return
    }

    // 后端返回的 token 保存到 localStorage。
    // 后续访问 /api/auth/me、/api/products 时，都要把它放进 Authorization 请求头。
    localStorage.setItem('token', result.data.token)

    currentUser.value = result.data.user

    // 登录成功后马上加载商品列表。
    // 这样页面会从登录卡片切换到“商品工作台”，并且能看到真实接口数据。
    await loadProducts()
  } catch (error) {
    errorMessage.value = '无法连接后端服务'
  } finally {
    loading.value = false
  }
}

// 测试 token 是否能访问 /api/auth/me
async function loadCurrentUser() {
  const token = localStorage.getItem('token')

  if (!token) {
    errorMessage.value = '请先登录'
    return
  }

  const response = await fetch('/api/auth/me', {
    headers: {
      Authorization: `Bearer ${token}`,
    },
  })

  const result = await response.json()

  if (result.code === 0) {
    currentUser.value = result.data
    await loadProducts()
  } else {
    errorMessage.value = result.message || '获取当前用户失败'
  }
}

// 加载商品列表。
// 这个方法会请求后端 GET /api/products，接口需要带 JWT token。
async function loadProducts() {
  const token = localStorage.getItem('token')

  if (!token) {
    productError.value = '登录状态已失效，请重新登录'
    return
  }

  productLoading.value = true
  productError.value = ''

  try {
    // URLSearchParams 用来拼接查询参数。
    // 这样比手写 ?keyword=xxx&page=1 更安全，也更不容易写错。
    const params = new URLSearchParams({
      page: String(productPage.value),
      pageSize: String(productPageSize.value),
    })

    if (productKeyword.value.trim()) {
      params.set('keyword', productKeyword.value.trim())
    }

    // 状态筛选。
    // activeStatus 为 null 时表示“全部商品”，不传 status。
    // activeStatus 为 1 时只查启用商品，为 0 时只查停用商品。
    if (activeStatus.value !== null) {
      params.set('status', String(activeStatus.value))
    }

    // 分类筛选。
    // activeCategory 为空时表示“全部分类”，不传 category。
    if (activeCategory.value) {
      params.set('category', activeCategory.value)
    }

    const response = await fetch(`/api/products?${params.toString()}`, {
      headers: {
        Authorization: `Bearer ${token}`,
      },
    })

    const result = await response.json()

    if (result.code !== 0) {
      productError.value = result.message || '商品列表加载失败'
      return
    }

    products.value = result.data.records
    productTotal.value = result.data.total

    // 后端会返回经过安全修正后的 page/pageSize。
    // 例如前端传 page=0，后端会修正为 page=1。
    // 这里同步回来，可以保证前端显示的页码和后端实际查询一致。
    productPage.value = result.data.page
    productPageSize.value = result.data.pageSize
  } catch (error) {
    productError.value = '无法连接商品接口'
  } finally {
    productLoading.value = false
  }
}

// 执行商品搜索。
// 搜索按钮和输入框 Enter 都调用这个方法，让交互入口保持一致。
async function searchProducts() {
  // 去掉用户输入前后的空格，避免 " 咖啡 " 这种输入导致搜索不准确。
  productKeyword.value = productKeyword.value.trim()

  // 新搜索应该从第一页开始。
  // 如果用户之前停在第 2 页，搜索后继续查第 2 页，可能会错过搜索结果。
  productPage.value = 1
  closeProductDetail()

  // 真正请求后端的逻辑仍然放在 loadProducts 里。
  // loadProducts 会读取 productKeyword，并自动拼接 keyword 查询参数。
  await loadProducts()
}

// 清空商品搜索条件。
// 清空后再次调用 loadProducts，此时不会携带 keyword 参数，列表会恢复为全部商品。
async function clearProductSearch() {
  productKeyword.value = ''
  productPage.value = 1
  closeProductDetail()
  await loadProducts()
}

// 切换商品状态筛选。
// 点击“全部商品 / 已启用 / 已停用”时执行。
async function selectStatusFilter(status: number | null) {
  activeStatus.value = status
  productPage.value = 1
  closeProductDetail()
  await loadProducts()
}

// 切换商品分类筛选。
// 点击左侧分类按钮时执行。
async function selectCategoryFilter(category: string) {
  activeCategory.value = category === '全部分类' ? '' : category
  productPage.value = 1
  closeProductDetail()
  await loadProducts()
}

// 清空搜索和筛选条件。
// 以后如果你想加 Reset 按钮，可以直接调用这个方法。
async function resetProductFilters() {
  productKeyword.value = ''
  activeStatus.value = null
  activeCategory.value = ''
  productPage.value = 1
  closeProductDetail()
  await loadProducts()
}

// 跳转到上一页。
// 只有当前页大于 1 时才允许跳转。
async function goToPreviousPage() {
  if (productPage.value <= 1) {
    return
  }

  productPage.value -= 1
  closeProductDetail()
  await loadProducts()
}

// 跳转到下一页。
// 只有当前页小于总页数时才允许跳转。
async function goToNextPage() {
  if (productPage.value >= productTotalPages.value) {
    return
  }

  productPage.value += 1
  closeProductDetail()
  await loadProducts()
}

// 加载商品详情。
// 点击商品卡片时调用这个方法，它会请求 GET /api/products/{id}。
async function loadProductDetail(id: number) {
  const token = localStorage.getItem('token')

  if (!token) {
    detailError.value = '登录状态已失效，请重新登录'
    return
  }

  detailLoading.value = true
  detailError.value = ''
  selectedProduct.value = null

  try {
    const response = await fetch(`/api/products/${id}`, {
      headers: {
        Authorization: `Bearer ${token}`,
      },
    })

    const result = await response.json()

    if (result.code !== 0) {
      detailError.value = result.message || '商品详情加载失败'
      return
    }

    selectedProduct.value = result.data
  } catch (error) {
    detailError.value = '无法连接商品详情接口'
  } finally {
    detailLoading.value = false
  }
}

// 关闭商品详情页，回到商品列表。
function closeProductDetail() {
  selectedProduct.value = null
  detailError.value = ''
  detailLoading.value = false
}

// 退出登录。
// 前端删除 token 后，把 currentUser 清空，页面自然会回到登录页。
function logout() {
  localStorage.removeItem('token')
  currentUser.value = null
  products.value = []
  productTotal.value = 0
  productPage.value = 1
  productError.value = ''
  closeProductDetail()
}
</script>

<template>
  <div class="page">
    <header class="topbar">
      <div class="brand">
        <span class="brand-icon">📦</span>
        <span>Inventory System</span>
      </div>

      <nav class="nav">
        <a>Products</a>
        <a>Warehouses</a>
        <a>Inventory</a>
        <button v-if="!currentUser" class="signup-button">Sign Up</button>
        <button v-else class="avatar-button" :title="`${currentUser.nickname}，点击退出登录`" @click="logout">
          <img v-if="currentUser.avatarUrl" :src="currentUser.avatarUrl" :alt="currentUser.nickname" />
          <span v-else>{{ currentUser.nickname.slice(0, 1) }}</span>
        </button>
      </nav>
    </header>

    <main v-if="!currentUser" class="login-area">
      <section class="login-card">
        <div class="mascot">📦</div>

        <h1>Log In</h1>
        <p class="subtitle">With your Inventory Account</p>

        <label>
          <span>Username</span>
          <input v-model="username" placeholder="Username" />
        </label>

        <label>
          <span>Password</span>
          <input v-model="password" type="password" placeholder="Password" />
        </label>

        <p v-if="errorMessage" class="error">{{ errorMessage }}</p>

        <button class="login-button" :disabled="loading" @click="handleLogin">
          {{ loading ? 'Logging in...' : 'Login' }}
        </button>

        <button class="secondary-button" @click="loadCurrentUser">
          Test /api/auth/me
        </button>

      </section>
    </main>

    <main v-else class="workspace">
      <section class="models-layout">
        <aside class="filter-sidebar">
          <div class="filter-tabs">
            <button class="active">Main</button>
            <button>Categories</button>
            <button>Status</button>
          </div>

          <div class="filter-group">
            <h3>Status</h3>
            <div class="chip-grid">
              <button
                v-for="item in statusFilters"
                :key="item.label"
                class="filter-chip"
                :class="{ selected: activeStatus === item.value }"
                @click="selectStatusFilter(item.value)"
              >
                {{ item.label }}
              </button>
            </div>
          </div>

          <div class="filter-group">
            <h3>Categories</h3>
            <div class="chip-grid">
              <button
                v-for="item in categoryFilters"
                :key="item"
                class="filter-chip"
                :class="{ selected: activeCategory === item || (!activeCategory && item === '全部分类') }"
                @click="selectCategoryFilter(item)"
              >
                {{ item }}
              </button>
            </div>
          </div>

          <div class="filter-group">
            <h3>Tech Stack</h3>
            <div class="chip-grid">
              <button v-for="item in libraryFilters" :key="item" class="filter-chip">
                {{ item }}
              </button>
            </div>
          </div>
        </aside>

        <section class="model-content">
          <template v-if="selectedProduct || detailLoading || detailError">
            <div class="detail-toolbar">
              <button class="back-button" @click="closeProductDetail">
                Back to products
              </button>
            </div>

            <div v-if="detailLoading" class="loading-card">
              Loading product detail...
            </div>

            <p v-else-if="detailError" class="dashboard-error">{{ detailError }}</p>

            <article v-else-if="selectedProduct" class="detail-panel">
              <div class="detail-header">
                <div class="detail-avatar">
                  {{ selectedProduct.category ? selectedProduct.category.slice(0, 1) : 'P' }}
                </div>

                <div class="detail-title">
                  <h1>{{ selectedProduct.productName }}</h1>
                  <p>
                    {{ selectedProduct.productCode }}
                    <span>•</span>
                    {{ selectedProduct.category || '未分类' }}
                  </p>
                </div>

                <span class="status-badge detail-status" :class="{ off: selectedProduct.status === 0 }">
                  {{ selectedProduct.status === 1 ? 'Enabled' : 'Disabled' }}
                </span>
              </div>

              <div class="detail-grid">
                <section class="detail-section">
                  <h2>Product Info</h2>

                  <dl class="detail-list">
                    <div>
                      <dt>Product Code</dt>
                      <dd>{{ selectedProduct.productCode }}</dd>
                    </div>
                    <div>
                      <dt>Category</dt>
                      <dd>{{ selectedProduct.category || '未分类' }}</dd>
                    </div>
                    <div>
                      <dt>Unit</dt>
                      <dd>{{ selectedProduct.unit }}</dd>
                    </div>
                    <div>
                      <dt>Safe Stock</dt>
                      <dd>{{ selectedProduct.safeStock }} {{ selectedProduct.unit }}</dd>
                    </div>
                  </dl>
                </section>

                <section class="detail-section">
                  <h2>Images</h2>

                  <div v-if="selectedProduct.images.length === 0" class="image-empty">
                    暂无商品图片
                  </div>

                  <div v-else class="image-list">
                    <div v-for="image in selectedProduct.images" :key="image.id" class="image-card">
                      <div class="image-placeholder">
                        {{ image.isMain === 1 ? 'Main' : `#${image.sortOrder}` }}
                      </div>
                      <p>{{ image.imageUrl }}</p>
                    </div>
                  </div>
                </section>
              </div>
            </article>
          </template>

          <template v-else>
            <div class="model-toolbar">
              <div class="model-title">
                <h1>Products</h1>
                <span>{{ productTotal }}</span>
              </div>

              <div class="toolbar-actions">
                <input
                  v-model="productKeyword"
                  class="model-search"
                  placeholder="Filter by product name or code"
                  @keyup.enter="searchProducts"
                />
                <button class="search-button" :disabled="productLoading" @click="searchProducts">
                  Search
                </button>
                <button class="clear-button" :disabled="productLoading || !productKeyword" @click="clearProductSearch">
                  Clear
                </button>
                <button
                  class="clear-button"
                  :disabled="productLoading || (!productKeyword && activeStatus === null && !activeCategory)"
                  @click="resetProductFilters"
                >
                  Reset filters
                </button>
                <button class="ghost-pill">Base only</button>
                <button class="ghost-pill active">Stock Available</button>
                <button class="ghost-pill">Sort: Latest</button>
              </div>
            </div>

            <p v-if="productError" class="dashboard-error">{{ productError }}</p>

            <div v-if="productLoading" class="loading-card">
              Loading products...
            </div>

            <div v-else-if="products.length === 0" class="empty-card">
              还没有查询到商品。你可以先确认数据库里是否已经有商品数据。
            </div>

            <div v-else class="product-list">
              <article
                v-for="product in products"
                :key="product.id"
                class="product-card"
                role="button"
                tabindex="0"
                @click="loadProductDetail(product.id)"
                @keyup.enter="loadProductDetail(product.id)"
              >
                <div class="product-icon">
                  {{ product.category ? product.category.slice(0, 1) : 'P' }}
                </div>

                <div class="product-main">
                  <h2>{{ product.productName }}</h2>
                  <p>
                    {{ product.productCode }}
                    <span>•</span>
                    {{ product.category || '未分类' }}
                    <span>•</span>
                    安全库存 {{ product.safeStock }} {{ product.unit }}
                    <span>•</span>
                    {{ product.createdAt }}
                  </p>
                </div>

                <span class="status-badge" :class="{ off: product.status === 0 }">
                  {{ product.status === 1 ? 'Enabled' : 'Disabled' }}
                </span>
              </article>
            </div>

            <div v-if="productTotal > 0" class="pagination-bar">
              <p>
                Showing {{ productStartIndex }}-{{ productEndIndex }} of {{ productTotal }}
              </p>

              <div class="pagination-actions">
                <button :disabled="productLoading || productPage <= 1" @click="goToPreviousPage">
                  Previous
                </button>
                <span>Page {{ productPage }} / {{ productTotalPages }}</span>
                <button :disabled="productLoading || productPage >= productTotalPages" @click="goToNextPage">
                  Next
                </button>
              </div>
            </div>
          </template>
        </section>
      </section>
    </main>
  </div>
</template>
