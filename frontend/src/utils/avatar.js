/**
 * 头像生成工具
 */

// 渐变色方案
const gradients = [
  'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
  'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)',
  'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)',
  'linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)',
  'linear-gradient(135deg, #fa709a 0%, #fee140 100%)',
  'linear-gradient(135deg, #30cfd0 0%, #330867 100%)',
  'linear-gradient(135deg, #a8edea 0%, #fed6e3 100%)',
  'linear-gradient(135deg, #ff9a9e 0%, #fecfef 100%)',
  'linear-gradient(135deg, #ffecd2 0%, #fcb69f 100%)',
  'linear-gradient(135deg, #ff6e7f 0%, #bfe9ff 100%)',
  'linear-gradient(135deg, #e0c3fc 0%, #8ec5fc 100%)',
  'linear-gradient(135deg, #f77062 0%, #fe5196 100%)',
  'linear-gradient(135deg, #fbc2eb 0%, #a6c1ee 100%)',
  'linear-gradient(135deg, #fdcbf1 0%, #e6dee9 100%)',
  'linear-gradient(135deg, #a1c4fd 0%, #c2e9fb 100%)',
  'linear-gradient(135deg, #d299c2 0%, #fef9d7 100%)',
  'linear-gradient(135deg, #89f7fe 0%, #66a6ff 100%)',
  'linear-gradient(135deg, #e52d27 0%, #b31217 100%)',
  'linear-gradient(135deg, #2af598 0%, #009efd 100%)',
  'linear-gradient(135deg, #ee9ca7 0%, #ffdde1 100%)'
]

// 可爱的emoji表情
const emojis = [
  '🦊', '🐱', '🐶', '🐼', '🐨', '🐰', '🦁', '🐯', '🐮', '🐷',
  '🐸', '🐵', '🐔', '🐧', '🐦', '🦄', '🐝', '🦋', '🐳', '🐬',
  '🌸', '🌺', '🌻', '🌷', '🌹', '💐', '🌈', '⭐', '💫', '✨',
  '🎨', '🎭', '🎪', '🎬', '🎮', '🎯', '🎲', '🎼', '🎵', '🎸',
  '🍎', '🍇', '🍊', '🍋', '🍌', '🍉', '🍓', '🍑', '🍒', '🥝'
]

/**
 * 根据字符串生成一个稳定的索引值
 * @param {String} str 
 * @returns {Number}
 */
function hashCode(str) {
  if (!str) return 0
  let hash = 0
  for (let i = 0; i < str.length; i++) {
    const char = str.charCodeAt(i)
    hash = ((hash << 5) - hash) + char
    hash = hash & hash // Convert to 32bit integer
  }
  return Math.abs(hash)
}

/**
 * 生成默认头像URL (使用DiceBear API)
 * @param {String|Number} userId 用户ID
 * @param {String} style 头像风格: adventurer, avataaars, bottts, fun-emoji, lorelei, micah, open-peeps, personas
 * @returns {String}
 */
export function getDefaultAvatar(userId, style = 'fun-emoji') {
  if (!userId) {
    userId = Date.now() + Math.random()
  }
  const seed = encodeURIComponent(String(userId))
  return `https://api.dicebear.com/7.x/${style}/svg?seed=${seed}&size=200`
}

/**
 * 根据用户ID获取渐变色
 * @param {String|Number} userId 
 * @returns {String}
 */
export function getGradientByUserId(userId) {
  const hash = hashCode(String(userId))
  const index = hash % gradients.length
  return gradients[index]
}

/**
 * 根据用户ID获取emoji
 * @param {String|Number} userId 
 * @returns {String}
 */
export function getEmojiByUserId(userId) {
  const hash = hashCode(String(userId))
  const index = hash % emojis.length
  return emojis[index]
}

/**
 * 生成SVG格式的默认头像（渐变色+emoji）
 * @param {String|Number} userId 
 * @param {Number} size 
 * @returns {String} data:image/svg+xml格式的图片
 */
export function generateSvgAvatar(userId, size = 200) {
  const gradient = getGradientByUserId(userId)
  const emoji = getEmojiByUserId(userId)
  
  // 提取渐变色中的颜色
  const gradientMatch = gradient.match(/#[0-9a-f]{6}/gi)
  const color1 = gradientMatch?.[0] || '#667eea'
  const color2 = gradientMatch?.[1] || '#764ba2'
  
  const svg = `
    <svg width="${size}" height="${size}" xmlns="http://www.w3.org/2000/svg">
      <defs>
        <linearGradient id="grad" x1="0%" y1="0%" x2="100%" y2="100%">
          <stop offset="0%" style="stop-color:${color1};stop-opacity:1" />
          <stop offset="100%" style="stop-color:${color2};stop-opacity:1" />
        </linearGradient>
      </defs>
      <rect width="${size}" height="${size}" fill="url(#grad)" rx="${size / 10}"/>
      <text x="50%" y="50%" text-anchor="middle" dy=".35em" font-size="${size * 0.5}" fill="white" opacity="0.9">
        ${emoji}
      </text>
    </svg>
  `
  
  return `data:image/svg+xml;charset=utf-8,${encodeURIComponent(svg.trim())}`
}

/**
 * 获取用户头像（优先使用真实头像，否则生成默认头像）
 * @param {Object} user 用户对象
 * @param {String} type 默认头像类型: 'svg' | 'dicebear' 
 * @returns {String}
 */
export function getUserAvatar(user, type = 'svg') {
  // 如果用户有真实头像且不是默认头像路径，则使用真实头像
  if (user?.avatar && !user.avatar.includes('default-avatar')) {
    return user.avatar
  }
  
  // 否则根据用户ID生成默认头像
  const userId = user?.id || user?.openid || Date.now()
  
  if (type === 'dicebear') {
    return getDefaultAvatar(userId, 'fun-emoji')
  }
  
  return generateSvgAvatar(userId)
}

/**
 * 获取所有可用的头像风格列表
 * @returns {Array}
 */
export function getAvatarStyles() {
  return [
    { value: 'adventurer', label: '冒险家' },
    { value: 'avataaars', label: '卡通风' },
    { value: 'bottts', label: '机器人' },
    { value: 'fun-emoji', label: '趣味表情' },
    { value: 'lorelei', label: '现代风' },
    { value: 'micah', label: '简约风' },
    { value: 'open-peeps', label: '涂鸦风' },
    { value: 'personas', label: '人物肖像' }
  ]
}

