const imageModules = import.meta.glob('../img/*', {
  eager: true,
  import: 'default',
})

const assetMap = Object.fromEntries(
  Object.entries(imageModules).map(([modulePath, assetUrl]) => [
    modulePath.split('/').pop(),
    assetUrl,
  ])
)

const legacyAliasMap = {
  'banner-1.png': 'banner_01.jpeg',
  'banner-2.png': 'banner_02.jpeg',
  'banner-3.png': 'banner_03.jpeg',
  'banner-4.png': 'banner_04.png',
  'banner-5.png': 'banner_05.png',
  'banner-6.png': 'banner_06.png',
  'banner-7.png': 'banner_07.png',
  'doctor-male-doc.jpg': 'doctor-male-doc.jpg',
  'doctor-female-doc.jpg': 'doctor-female-doc.jpg',
  'default-avatar.png': 'icons-pa.jpg',
  'hospital.jpg': 'hospital_100001977.jpg',
  'health-1.png': 'health_01.jpeg',
  'health-2.png': 'health_02.jpeg',
  'health-3.png': 'health_03.jpeg',
  'health_01.jpeg': 'health_01.jpeg',
  'health_02.jpeg': 'health_02.jpeg',
  'health_03.jpeg': 'health_03.jpeg',
  'hospital_100001977.jpg': 'hospital_100001977.jpg',
  'hospital_14616536073112.jpg': 'hospital_14616536073112.jpg',
  'hospital_16043929044175.jpg': 'hospital_16043929044175.jpg',
  'hospital_16195932543164.png': 'hospital_16195932543164.png',
  'user-1.png': 'icons-pa.jpg',
}

const stripQuery = (value) => value.split('?')[0].split('#')[0]

const normalizeKey = (value) => {
  const raw = stripQuery(String(value || '').trim())
  if (!raw) return ''

  return raw
    .replace(/^https?:\/\/[^/]+\//, '')
    .replace(/^\.?\//, '')
    .replace(/^src\//, '')
    .replace(/^assets\//, '')
    .replace(/^img\//, '')
    .replace(/^avatar\//, '')
    .replace(/^\//, '')
}

export const DEFAULT_IMAGES = {
  banner: assetMap['banner_01.jpeg'],
  hospital: assetMap['hospital_100001977.jpg'],
  doctor: assetMap['doctor-male-doc.jpg'],
  article: assetMap['health_01.jpeg'],
  avatar: assetMap['icons-pa.jpg'],
}

export function resolveImageUrl(value, fallback = '') {
  if (!value) {
    return assetMap[fallback] || fallback || ''
  }

  const raw = String(value).trim()
  if (/^(https?:|data:|blob:)/i.test(raw)) {
    return raw
  }

  const normalized = normalizeKey(raw)
  const fileName = normalized.split('/').pop()
  const mappedFileName = legacyAliasMap[normalized] || legacyAliasMap[fileName] || fileName

  return assetMap[mappedFileName] || assetMap[fallback] || raw
}