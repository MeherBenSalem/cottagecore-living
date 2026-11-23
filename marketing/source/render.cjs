// Renders the listing artwork in marketing/images/ from real in-game screenshots, isometric renders of the mod's own
// models (marketing/source/renders/, made by renders.py), the mod logo and the bundled OFL fonts. Usage (from the
// repository root):
//   NODE_PATH="$(npm root -g)" node marketing/source/render.cjs [name...]
// Requires the `playwright` package with a Chromium build available. Set CHROMIUM to a Chromium executable to use
// that one instead of Playwright's own download.
const path = require('path');
const fs = require('fs');
const { chromium } = require('playwright');

// The release and the supported Minecraft versions. Adding a version is one line here
// (plus one row in the compatibility tables of curseforge/description.md and modrinth/description.md).
const RELEASE = { version: '1.0' };
const VERSIONS = [
  { minecraft: '1.20.1', loaders: ['Forge'] },
];
const LOADERS = ['Fabric', 'Forge', 'NeoForge'].filter((loader) => VERSIONS.some((v) => v.loaders.includes(loader)));

const root = path.resolve(__dirname, '..', '..');
const out = path.join(root, 'marketing', 'images');
const shots = path.join(out, 'screenshots');
const fonts = path.join(__dirname, 'fonts');
const renders = path.join(__dirname, 'renders');
const url = (file) => 'file://' + file;
const shot = (name) => url(path.join(shots, name));
const source = (name) => url(path.join(__dirname, name));

// The five sets, their colours and names from catalog/DecorSet.java and the en_us lang file.
const SETS = [
  ['strawberry', 'Strawberry Cottage', '#E5484D', 'Red gingham, roses and daisies'],
  ['lavender', 'Lavender Home', '#8364C2', 'Lavender gingham and lavender sprigs'],
  ['pink', 'Pink Garden', '#D9668E', 'Floral print and peonies'],
  ['fairy', 'Fairycore', '#5FAE8C', 'Toadstool print, bluebells, mushroom roofs'],
  ['vintage', 'Vintage Cottage', '#5D8576', 'Plaid, rose sprigs and brass'],
];
const SEASONS = [['spring', 'Spring', '#E9A3C0'], ['summer', 'Summer', '#E8B93C'], ['autumn', 'Autumn', '#D9772E'], ['winter', 'Winter', '#7FA9C9']];

const css = `
@font-face { font-family: ClDisplay; src: url('${url(path.join(fonts, 'fredoka.ttf'))}'); }
@font-face { font-family: ClText; src: url('${url(path.join(fonts, 'nunito.ttf'))}'); font-weight: 400; }
@font-face { font-family: ClText; src: url('${url(path.join(fonts, 'nunito-extrabold.ttf'))}'); font-weight: 800; }
:root { --text:#4f4636; --dim:#8f8470; --bark:#5a4128; --berry:#E5484D; --berry-deep:#c4363c; --sage:#7fa37a; --sage-deep:#5D8576;
        --lav:#8364C2; --rose:#D9668E; --mint:#5FAE8C; --butter:#f2cf6b; --cream:#fffbf0; --line:rgba(196,54,60,.28); }
* { box-sizing: border-box; }
html, body { margin: 0; background: var(--cream); color: var(--text); font-family: ClText, sans-serif; }
.stage { position: relative; overflow: hidden; background: linear-gradient(160deg, #fffbf0 0%, #fbf3e6 55%, #eef3e2 100%); }
.display { font-family: ClDisplay, sans-serif; }
.berry { background: linear-gradient(100deg, #d93b43 0%, #e5636a 38%, #c97a9a 70%, #8a6cc4 100%);
         -webkit-background-clip: text; background-clip: text; color: transparent; }
.soft { filter: drop-shadow(0 3px 0 rgba(255,255,255,.95)) drop-shadow(0 10px 24px rgba(196,54,60,.22)); }
.kicker { font-family: ClDisplay, sans-serif; color: var(--sage-deep); letter-spacing: .14em; font-size: 24px; text-transform: uppercase; }
.daisy { display: inline-block; vertical-align: -4px; margin-right: 12px; }
.gingham { background-color: #fff; background-image:
  linear-gradient(90deg, rgba(229,72,77,.5) 50%, transparent 50%), linear-gradient(rgba(229,72,77,.5) 50%, transparent 50%);
  background-size: 16px 16px; }
.rule { height: 6px; border-radius: 3px; background: repeating-linear-gradient(90deg, var(--berry) 0 10px, transparent 10px 16px, var(--sage) 16px 26px, transparent 26px 32px); opacity: .75; }
.frame { position: relative; border: 7px solid #fff; border-radius: 22px; overflow: hidden; background: #fff;
         box-shadow: 0 0 0 2px rgba(127,163,122,.55), 0 26px 60px rgba(90,65,40,.25), 0 6px 14px rgba(90,65,40,.12); }
.frame > img { display: block; width: 100%; }
canvas.motes { position: absolute; left: 0; top: 0; width: 100%; height: 100%; pointer-events: none; }
.wash { position: absolute; inset: 0; background: radial-gradient(ellipse at 70% 45%, rgba(255,255,255,0) 30%, rgba(255,250,238,.6) 100%); }
.backdrop { position: absolute; inset: -40px; background-size: cover; background-position: center; filter: blur(18px) brightness(1.12) saturate(1.05); }
.pill { display: inline-block; border-radius: 999px; padding: 9px 22px 7px; margin-right: 12px; font-family: ClDisplay, sans-serif; font-size: 22px;
        color: #fff; background: linear-gradient(100deg, #e5484d, #d9668e); box-shadow: 0 6px 16px rgba(196,54,60,.28); }
.pill.ghost { color: var(--sage-deep); background: rgba(255,255,255,.88); box-shadow: 0 0 0 2px rgba(93,133,118,.55), 0 6px 16px rgba(90,65,40,.12); }
.label { position: absolute; font-family: ClDisplay, sans-serif; font-size: 23px; color: var(--bark); background: rgba(255,255,255,.95);
         border-radius: 999px; padding: 8px 20px 6px; white-space: nowrap; box-shadow: 0 0 0 2px rgba(127,163,122,.7), 0 10px 26px rgba(90,65,40,.25); z-index: 2; }
ul.points { list-style: none; padding: 0; margin: 0; }
ul.points li { position: relative; padding-left: 38px; margin: 0 0 16px; font-size: 27px; line-height: 1.3; color: var(--text); }
ul.points li::before { content: ''; position: absolute; left: 0; top: 7px; width: 22px; height: 22px; background: var(--berry);
  -webkit-mask: var(--flower) center / contain no-repeat; mask: var(--flower) center / contain no-repeat; }
ul.points li:nth-child(3n+2)::before { background: var(--lav); }
ul.points li:nth-child(3n)::before { background: var(--mint); }
:root { --flower: url("data:image/svg+xml;utf8,<svg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24'><g><circle cx='12' cy='5' r='4.6'/><circle cx='18.7' cy='9.8' r='4.6'/><circle cx='16.1' cy='17.7' r='4.6'/><circle cx='7.9' cy='17.7' r='4.6'/><circle cx='5.3' cy='9.8' r='4.6'/></g></svg>"); }
b.hi { color: var(--bark); font-weight: 800; }
.stats { display: flex; gap: 14px; margin-top: 22px; }
.stats > div { flex: 1; padding: 14px 20px 12px; border-radius: 18px; background: rgba(255,255,255,.93);
               box-shadow: 0 0 0 2px rgba(127,163,122,.45), 0 14px 34px rgba(90,65,40,.16); text-align: center; }
.stats .t { font-family: ClText, sans-serif; font-weight: 800; font-size: 16px; letter-spacing: .1em; text-transform: uppercase; color: var(--dim); }
.stats .v { font-family: ClDisplay, sans-serif; font-size: 34px; color: var(--bark); margin-top: 2px; white-space: nowrap; }
.tile { position: relative; border-radius: 18px; background: rgba(255,255,255,.93); text-align: center;
        box-shadow: 0 0 0 2px rgba(127,163,122,.45), 0 14px 30px rgba(90,65,40,.16); }
.tile .art { display: flex; align-items: center; justify-content: center; }
.tile .art img { image-rendering: pixelated; max-width: 100%; max-height: 100%; }
.tile .n { font-family: ClDisplay, sans-serif; color: var(--bark); line-height: 1.1; }
.tile .s { font-family: ClText, sans-serif; font-weight: 800; color: var(--dim); font-size: 15px; letter-spacing: .04em; }
pre.code { margin: 0; font-family: 'DejaVu Sans Mono', monospace; font-size: 22px; line-height: 1.5; color: var(--text); }
`;

// A five-petal flower with a butter centre, the cottage counterpart of the sibling kits' hearts and stars.
const flowerSvg = (fill, size, centre = '#f2cf6b') => `<svg class="daisy" width="${size}" height="${size}" viewBox="0 0 24 24" style="display:inline-block">
  <g fill="${fill}" stroke="#fff" stroke-width="1.2"><circle cx="12" cy="5" r="4.6"/><circle cx="18.7" cy="9.8" r="4.6"/><circle cx="16.1" cy="17.7" r="4.6"/>
  <circle cx="7.9" cy="17.7" r="4.6"/><circle cx="5.3" cy="9.8" r="4.6"/></g><circle cx="12" cy="12" r="3.6" fill="${centre}" stroke="#fff" stroke-width="1.2"/></svg>`;

// Drifting blossom petals and tiny daisies. Deterministic so renders are reproducible.
const motes = (count = 40, seed = 7) => `<canvas class="motes"></canvas><script>
(() => { const c = document.currentScript.previousElementSibling; const r = c.getBoundingClientRect();
  c.width = r.width; c.height = r.height; const g = c.getContext('2d'); let s = ${seed};
  const rnd = () => (s = (s * 16807) % 2147483647) / 2147483647;
  const daisy = (x, y, z, a) => { g.save(); g.translate(x, y); g.fillStyle = 'rgba(255,255,255,' + a + ')';
    for (let i = 0; i < 5; i++) { const t = i * Math.PI * 2 / 5; g.beginPath(); g.arc(Math.sin(t) * z * 1.6, -Math.cos(t) * z * 1.6, z * 1.3, 0, Math.PI * 2); g.fill(); }
    g.fillStyle = 'rgba(242,207,107,' + a + ')'; g.beginPath(); g.arc(0, 0, z, 0, Math.PI * 2); g.fill(); g.restore(); };
  const petal = (x, y, z, a, rot, tint) => { g.save(); g.translate(x, y); g.rotate(rot);
    const h = g.createLinearGradient(-z * 4, 0, z * 4, 0); h.addColorStop(0, 'rgba(' + tint + ',' + a + ')'); h.addColorStop(1, 'rgba(255,246,240,' + a + ')');
    g.fillStyle = h; g.beginPath(); g.ellipse(0, 0, z * 4.2, z * 2.4, 0, 0, Math.PI * 2); g.fill(); g.restore(); };
  for (let i = 0; i < ${count}; i++) { const x = rnd() * c.width, y = rnd() * c.height, a = .4 + rnd() * .45, z = 1.4 + rnd() * 2.2;
    if (rnd() < .3) daisy(x, y, z, a); else petal(x, y, z, a, rnd() * Math.PI, rnd() < .5 ? '233,150,170' : '190,214,170'); } })();
</script>`;

// Isometric renders of the mod's own models (renders.py), drawn pixel-sharp. Waits for every image to load.
const loadScript = `<script>
(() => { const imgs = [...document.images]; let left = imgs.length;
  const done = () => { if (--left <= 0) document.body.dataset.items = 'ready'; };
  if (!left) document.body.dataset.items = 'ready';
  for (const i of imgs) { if (i.complete) done(); else { i.onload = done; i.onerror = done; } } })();
</script>`;
const model = (name, h) => `<img src="${url(path.join(renders, `${name}.png`))}" style="height:${h}px;image-rendering:pixelated">`;
// A tile with a model render and a caption.
const tile = (name, title, { h = 120, art = 140, size = 20, sub = '' } = {}) => `<div class="tile" style="padding:12px 8px 12px">
    <div class="art" style="height:${art}px">${model(name, h)}</div>
    <div class="n" style="font-size:${size}px;margin-top:6px">${title}</div>${sub ? `<div class="s">${sub}</div>` : ''}</div>`;
const grid = (cols, items, gap = 14) => `<div style="display:grid;grid-template-columns:repeat(${cols},1fr);gap:${gap}px">${items.join('')}</div>`;

const page = (width, height, body, transparent = false) => `<!doctype html><html><head><meta charset="utf-8"><style>${css}
body { width: ${width}px; height: ${height}px; } .stage { width: ${width}px; height: ${height}px; }
${transparent ? 'html, body, .stage { background: transparent; }' : ''}</style></head>
<body><div class="stage">${body}</div>${loadScript}</body></html>`;

// A section heading: a gingham-edged ribbon with the title, readable on CurseForge's and Modrinth's light and dark themes.
const header = (title) => page(1400, 120, `
  <div style="position:absolute;inset:0;display:flex;align-items:center;justify-content:center;gap:22px">
    <div class="rule" style="width:230px;-webkit-mask-image:linear-gradient(90deg,transparent,#000);mask-image:linear-gradient(90deg,transparent,#000)"></div>
    ${flowerSvg('#8364C2', 34)}
    <div style="position:relative;padding:14px 54px 12px;border-radius:999px;background:linear-gradient(100deg,#e5484d 0%,#e2697a 45%,#d9668e 100%);
                box-shadow:0 0 0 4px #fff,0 0 0 6px rgba(93,133,118,.7),0 12px 28px rgba(90,65,40,.28)">
      <div class="display" style="font-size:46px;line-height:1;color:#fff;white-space:nowrap;text-shadow:0 3px 0 rgba(150,40,50,.4)">${title}</div></div>
    ${flowerSvg('#5FAE8C', 34)}
    <div class="rule" style="width:230px;-webkit-mask-image:linear-gradient(90deg,#000,transparent);mask-image:linear-gradient(90deg,#000,transparent)"></div>
  </div>`, true);

// A feature card: copy on the left, artwork on the right. Bullets never end on a one-word line.
const card = ({ kicker, title, points, art, width = 1600, height = 900, artWidth = 980 }) => page(width, height, `
  <div class="backdrop" style="background-image:url('${source('backdrop.jpg')}');opacity:.45"></div>
  <div style="position:absolute;inset:0;background:linear-gradient(90deg,rgba(255,251,240,.95) 0%,rgba(255,251,240,.88) 34%,rgba(248,246,232,.6) 62%,rgba(240,244,228,.42) 100%)"></div>
  <div class="wash"></div>${motes()}
  <div class="gingham" style="position:absolute;left:0;right:0;top:0;height:14px;opacity:.8"></div>
  <div class="gingham" style="position:absolute;left:0;right:0;bottom:0;height:14px;opacity:.8"></div>
  <div style="position:absolute;left:76px;top:0;bottom:0;width:${width - artWidth - 176}px;display:flex;flex-direction:column;justify-content:center">
    <div class="kicker"${kicker.length > 22 ? ' style="font-size:20px;letter-spacing:.1em"' : ''}>${flowerSvg('#E5484D', 26)}${kicker}</div>
    <div class="display berry soft" style="font-size:66px;line-height:1.02;margin:16px 0 22px">${title}</div>
    <div class="rule" style="width:70%;margin-bottom:30px"></div>
    <ul class="points">${points.map((point) => `<li>${point.replace(/ (\S+)$/, '&nbsp;$1')}</li>`).join('')}</ul>
  </div>
  <div style="position:absolute;right:56px;top:50%;transform:translateY(-50%);width:${artWidth}px">${art}</div>`);

// An exact region (x, y, w, h in source pixels, 1280x720 by default) of a capture, scaled to W pixels wide.
const region = (file, x, y, w, h, W, full = 1280) => `<div style="position:relative;width:${W}px;height:${Math.round(W * h / w)}px;overflow:hidden">
    <img src="${shot(file)}" style="position:absolute;max-width:none;width:${W * full / w}px;left:${-x * W / w}px;top:${-y * W / w}px"></div>`;
const framed = (inner, style = '') => `<div class="frame" style="${style}">${inner}</div>`;
const label = (text, style) => `<div class="label" style="${style}">${text}</div>`;
const stats = (items) => `<div class="stats">${items.map(([t, v, flex]) =>
  `<div${flex ? ` style="flex:${flex}"` : ''}><div class="v">${v}</div><div class="t">${t}</div></div>`).join('')}</div>`;
// Two overlapping captures, the first top left and the second bottom right.
const pair = (first, second, firstLabel, secondLabel, height = 640, width = '73%') => `<div style="position:relative;height:${height}px">
    <div class="frame" style="position:absolute;left:0;top:0;width:${width}">${first}</div>
    <div class="frame" style="position:absolute;right:0;bottom:0;width:${width}">${second}</div>
    ${firstLabel ? label(firstLabel, 'left:24px;top:22px') : ''}${secondLabel ? label(secondLabel, 'right:24px;bottom:22px') : ''}</div>`;
const code = (file, body) => `<div class="frame" style="background:rgba(255,255,255,.96);padding:24px 34px">
  <div style="font-family:ClText;font-weight:800;font-size:18px;letter-spacing:.08em;color:var(--dim);margin-bottom:10px">${file}</div><pre class="code">${body}</pre></div>`;
const k = (t) => `<span style="color:#c4363c">${t}</span>`;
const v = (t) => `<span style="color:#5D8576">${t}</span>`;
const cm = (t) => `<span style="color:#b0a690">${t}</span>`;

const versionText = VERSIONS.map((ver) => ver.minecraft).join(' · ');

const assets = {
  banner: () => page(1800, 600, `
    <img src="${shot('parlour-closeup.jpg')}" style="position:absolute;right:-30px;top:-40px;width:1120px;-webkit-mask-image:linear-gradient(90deg,transparent 0%,#000 30%);mask-image:linear-gradient(90deg,transparent 0%,#000 30%)">
    <div style="position:absolute;inset:0;background:linear-gradient(90deg,rgba(255,251,240,.98) 0%,rgba(255,251,240,.96) 42%,rgba(255,251,240,.5) 60%,rgba(255,251,240,0) 76%)"></div>
    ${motes(46, 11)}
    <div class="gingham" style="position:absolute;left:0;right:0;top:0;height:16px"></div>
    <div class="gingham" style="position:absolute;left:0;right:0;bottom:0;height:16px"></div>
    <div style="position:absolute;left:86px;top:58px;width:1000px">
      <div class="kicker">${flowerSvg('#E5484D', 26)}Version ${RELEASE.version} · Sugarplum Studios</div>
      <div class="display berry soft" style="font-size:104px;line-height:1;white-space:nowrap;margin:16px 0 4px">Cottagecore Living</div>
      <div class="rule" style="width:640px;margin:18px 0 24px"></div>
      <div style="font-family:ClDisplay;font-size:42px;color:var(--bark);line-height:1.2">The little details.</div>
      <div style="font-size:27px;color:var(--text);line-height:1.35;margin-top:8px">285 cosy decorations for cottage interiors and gardens,<br>in five themed sets.</div>
      <div style="margin-top:30px">${LOADERS.map((l) => `<span class="pill">${l}</span>`).join('')}<span class="pill ghost">No dependencies</span></div>
      <div style="margin-top:16px;font-family:ClDisplay;font-size:26px;color:var(--dim)">Minecraft ${versionText}</div>
    </div>
`),

  icon: () => page(512, 512, `
    <div style="position:absolute;inset:0;background:radial-gradient(circle at 50% 40%,#fffbf0 0%,#f6efdc 55%,#e3edd3 100%)"></div>${motes(22, 5)}
    <div class="gingham" style="position:absolute;left:46px;top:46px;width:420px;height:420px;border-radius:50%;
                box-shadow:0 0 0 6px #fff,0 0 0 10px #7fa37a,0 24px 50px rgba(90,65,40,.3)"></div>
    <div style="position:absolute;left:74px;top:74px;width:364px;height:364px;border-radius:50%;background:radial-gradient(circle at 50% 45%,#fffdf7,#fbf3e2)"></div>
    <div style="position:absolute;left:74px;top:74px;width:364px;height:364px;display:flex;align-items:center;justify-content:center">
      ${model('strawberry_teapot', 230)}</div>
    ${[[256, 34], [478, 256], [256, 478], [34, 256]].map(([x, y], i) => `<div style="position:absolute;left:${x - 20}px;top:${y - 20}px">${flowerSvg(['#E5484D', '#8364C2', '#D9668E', '#5FAE8C'][i], 40)}</div>`).join('')}`),

  'card-collection': () => card({
    kicker: '285 decorations', title: 'The little<br>details',
    points: ['Books, candles, vases, clocks, curtains and mirrors', 'Birdhouses, wind chimes, fountains and fairy doors',
      'Decor for spring, summer, autumn and winter', 'Every piece is a block with its own 3D model', 'Many of them do something when you right-click'],
    art: grid(5, [['pink_stacked_books', 'Stacked Books'], ['lavender_candle_jar', 'Candle Jar'], ['strawberry_flower_vase', 'Flower Vase'],
      ['lavender_cuckoo_clock', 'Cuckoo Clock'], ['fairy_teapot', 'Teapot'], ['basket_of_strawberries', 'Strawberry Basket'],
      ['pink_birdhouse', 'Birdhouse'], ['fairy_wind_chime', 'Wind Chime'], ['lavender_mini_fountain', 'Mini Fountain'], ['vintage_garden_gnome', 'Garden Gnome'],
      ['blossom_branch_vase', 'Blossom Vase'], ['picnic_basket', 'Picnic Basket'], ['pumpkin_stack', 'Pumpkin Stack'], ['mini_snowman', 'Mini Snowman'],
      ['seasonal_tree@spring', 'Seasonal Tree']].map(([n, t]) => tile(n, t, { h: 96, art: 110, size: 18 })), 12) +
      stats([['Decorations', '285'], ['Kinds', '84'], ['Themed sets', '5'], ['Tablecloths', '5']]) }),

  'card-sets': () => card({
    kicker: 'Five themed sets', title: 'Pick your<br>palette',
    points: ['Strawberry Cottage, Lavender Home and Pink Garden', 'Fairycore and Vintage Cottage', '44 decorations and a tablecloth in each set',
      'Same recipe, one set ingredient to change it', 'Collect a whole set for a little celebration'],
    art: `<div style="display:flex;flex-direction:column;gap:12px">${SETS.map(([id, name, color, motif]) => `
      <div class="tile" style="display:flex;align-items:center;gap:14px;padding:8px 18px;text-align:left;box-shadow:0 0 0 3px ${color}66,0 12px 26px rgba(90,65,40,.15)">
        <div style="width:250px;flex:none"><div class="display" style="font-size:27px;color:${color};white-space:nowrap">${name}</div>
          <div class="s" style="font-size:14px">${motif}</div></div>
        <div style="flex:1;display:flex;align-items:flex-end;justify-content:space-between;height:118px;padding-bottom:6px">
          ${['teapot', 'flower_vase', 'cuckoo_clock', 'birdhouse', 'garden_lantern', 'garden_gnome', 'toadstools'].map((piece) => model(`${id}_${piece}`, 94)).join('')}</div>
      </div>`).join('')}</div>` }),

  'card-interior': () => card({
    kicker: 'Interior · 155 pieces', title: 'Cosy<br>corners',
    points: ['Stack books three high, light real candles', 'Clock hands follow the time of day', 'Trinket and jewelry boxes hold 9 slots',
      'Tables join and take a tablecloth', 'Curtains tie back, mirrors and frames hang'],
    art: framed(region('parlour-closeup.jpg', 140, 110, 1000, 520, 978)) + `<div style="margin-top:18px">${grid(7, [
      ['vintage_stacked_books', 'Books'], ['strawberry_pillar_candles', 'Candles'], ['pink_mantel_clock', 'Mantel Clock'], ['lavender_teacups', 'Teacups'],
      ['fairy_jewelry_box', 'Jewelry Box'], ['strawberry_vanity_mirror', 'Vanity Mirror'], ['cherry_cottage_table', 'Cottage Table']]
      .map(([n, t]) => tile(n, t, { h: 70, art: 80, size: 16 })), 10)}</div>` }),

  'card-garden': () => card({
    kicker: 'Garden · 89 pieces', title: 'A garden to<br>potter in',
    points: ['Birdhouses chirp and wind chimes ring in the breeze', 'Watering cans water the crops in front of them',
      'Fountains and the birdbath fill glass bottles', 'Picnic blankets join into one big blanket', 'Fairy doors, toadstools and garden gnomes'],
    art: grid(4, [['strawberry_birdhouse', 'Birdhouse', 'Chirps now and then'], ['lavender_wind_chime', 'Wind Chime', 'Rings in the breeze'],
      ['fairy_mini_fountain', 'Mini Fountain', 'Fills bottles'], ['pink_watering_can', 'Watering Can', 'Waters a 3×3 patch'],
      ['vintage_garden_lantern@floor_lit', 'Garden Lantern', 'Light 14'], ['strawberry_flower_wheelbarrow', 'Flower Wheelbarrow', 'A planter on wheels'],
      ['lavender_fairy_door', 'Fairy Door', 'Knock on it'], ['pink_garden_gnome', 'Garden Gnome', 'One for every set']]
      .map(([n, t, s]) => tile(n, t, { h: 150, art: 170, size: 22, sub: s })), 16) }),

  'card-seasons': () => card({
    kicker: 'Through the seasons', title: 'Decor that<br>changes',
    points: ['Five pieces follow the world’s season', 'Blossoms, lemons, acorns, then pine and snow', 'From the real calendar, every few days, or fixed',
      'Right-click to pick a season yourself', 'Plus 36 pieces for blossom, picnic, leaf and snow days'],
    art: `<div style="display:grid;grid-template-columns:repeat(4,1fr);gap:14px">${SEASONS.map(([id, name, color]) => `
      <div class="tile" style="padding:14px 8px 10px;box-shadow:0 0 0 3px ${color}88,0 14px 30px rgba(90,65,40,.16)">
        <div class="display" style="font-size:30px;color:${color};margin-bottom:6px">${name}</div>
        ${['seasonal_tree', 'seasonal_wreath', 'seasonal_centerpiece'].map((piece) => `<div class="art" style="height:150px">${model(`${piece}@${id}`, 130)}</div>`).join('')}
      </div>`).join('')}</div>` + stats([['Seasonal pieces', '41'], ['Seasons', '4'], ['Season modes', '3']]) }),

  'card-interactions': () => card({
    kicker: 'Right-click', title: 'Little things<br>they do',
    points: ['Cuckoo clocks call out every hour', 'Knock on a fairy door, sometimes there’s a gift', 'Tea and cocoa give 5 seconds of Regeneration',
      'Mirrors pay compliments, open books read a line', 'Shake a snow globe, swap a picture or a sign'],
    art: grid(4, [['vintage_cuckoo_clock', 'Cuckoo Clock', 'Calls the hour'], ['fairy_fairy_door@open', 'Fairy Door', 'Knock knock'],
      ['strawberry_teapot', 'Teapot', 'Warm and cosy'], ['lavender_vanity_mirror', 'Vanity Mirror', 'A compliment'],
      ['strawberry_wall_clock', 'Wall Clock', 'Tells the time'], ['pink_garden_sign', 'Garden Sign', 'Eight pictures'],
      ['picnic_basket@open', 'Picnic Basket', '9 slots'], ['firefly_jar', 'Firefly Jar', 'Glows, light 9']]
      .map(([n, t, s]) => tile(n, t, { h: 150, art: 170, size: 22, sub: s })), 16) }),

  'card-placing': () => card({
    kicker: 'Place it your way', title: 'Floor, wall<br>or ceiling',
    points: ['Floor pieces face you; sneak to turn them', 'Hang them on walls or from ceilings', 'Stack books, candles, jam jars and more, up to 3',
      'They sit right on blankets and carpets', 'Every decoration can be waterlogged'],
    art: framed(region('parlour.jpg', 250, 140, 860, 430, 978)) + `<div style="margin-top:18px">${grid(6, [
      ['fairy_pillar_candles', 'Stacks up', 'Floor'], ['strawberry_wall_clock', 'Wall Clock', 'Wall'], ['lavender_hanging_planter', 'Planter', 'Ceiling'],
      ['pink_curtains', 'Curtains', 'Window'], ['strawberry_picnic_blanket', 'Blanket', 'Joins'], ['oak_cottage_table', 'Table', 'Joins']]
      .map(([n, t, s]) => tile(n, t, { h: 70, art: 80, size: 17, sub: s })), 10)}</div>` }),

  'card-book': () => card({
    kicker: 'Decoration Collection Book', title: 'Collect<br>them all',
    points: ['Every decoration by collection or by set', 'Search, set filters and each piece’s recipe', 'Tracks what you craft, pick up, place or carry',
      'Saved with the player, in multiplayer too', 'Creative mode: take one, a stack or a whole set'],
    art: pair(region('book.jpg', 0, 0, 1280, 720, 715), region('book-sets.jpg', 0, 0, 1280, 720, 715), '', '', 720, '73%') }),

  'card-config': () => card({
    kicker: 'Make it yours', title: 'Set the<br>rules',
    points: ['Seasons from the calendar, a cycle, or fixed', 'Southern hemisphere calendar', 'Turn off crop watering, fountains, cosy effects',
      'Cuckoo calls, fairy gifts and the starter book', 'Ambient particles and sounds per player'],
    art: `<div style="display:flex;flex-direction:column;gap:24px">
      ${code('config/cottagecoreliving-common.toml', `${k('[seasons]')}
  ${k('mode')} = ${v('"CALENDAR"')}   ${cm('# CALENDAR, CYCLE or FIXED')}
  ${k('daysPerSeason')} = ${v('7')}
  ${k('southernHemisphere')} = ${v('false')}
${k('[interactions]')}
  ${k('wateringCanGrowsCrops')} = ${v('true')}
  ${k('cozyEffects')} = ${v('true')}
${k('[collection]')}
  ${k('giveBookOnFirstJoin')} = ${v('true')}`)}
      ${code('config/cottagecoreliving-client.toml', `${k('[display]')}
  ${k('ambientParticles')} = ${v('true')}
  ${k('ambientSounds')} = ${v('true')}`)}</div>` }),
};

const headers = { 'header-collection': 'The Collection', 'header-sets': 'Five Themed Sets', 'header-interior': 'Interior',
  'header-garden': 'Garden', 'header-seasons': 'Through the Seasons', 'header-interactions': 'Little Interactions',
  'header-placing': 'Placing & Arranging', 'header-book': 'The Collection Book', 'header-recipes': 'Crafting',
  'header-config': 'Configuration', 'header-everything': 'Everything Else', 'header-compatibility': 'Downloads & Compatibility',
  'header-faq': 'Questions', 'header-gallery': 'Gallery' };
for (const [name, title] of Object.entries(headers)) assets[name] = () => header(title);
assets.divider = () => page(1400, 40, `<div style="position:absolute;inset:0;display:flex;align-items:center;justify-content:center;gap:14px">
  <div class="rule" style="width:480px;-webkit-mask-image:linear-gradient(90deg,transparent,#000);mask-image:linear-gradient(90deg,transparent,#000)"></div>
  ${flowerSvg('#8364C2', 22)}${flowerSvg('#E5484D', 28)}${flowerSvg('#5FAE8C', 22)}
  <div class="rule" style="width:480px;-webkit-mask-image:linear-gradient(90deg,#000,transparent);mask-image:linear-gradient(90deg,#000,transparent)"></div></div>`, true);

(async () => {
  const wanted = process.argv.slice(2);
  const browser = await chromium.launch(process.env.CHROMIUM
    ? { executablePath: process.env.CHROMIUM, args: ['--no-sandbox', '--use-angle=swiftshader', '--enable-unsafe-swiftshader'] }
    : {});
  fs.mkdirSync(out, { recursive: true });
  for (const [name, build] of Object.entries(assets)) {
    if (wanted.length && !wanted.includes(name)) continue;
    const html = build();
    const size = html.match(/body \{ width: (\d+)px; height: (\d+)px; \}/);
    const tab = await browser.newPage({ viewport: { width: +size[1], height: +size[2] }, deviceScaleFactor: 1 });
    const file = path.join(__dirname, `.render-${name}.html`);
    fs.writeFileSync(file, html);
    await tab.goto(url(file));
    await tab.evaluate(() => document.fonts.ready);
    await tab.waitForLoadState('networkidle');
    await tab.waitForFunction(() => document.body.dataset.items === 'ready');
    await tab.screenshot({ path: path.join(out, `${name}.png`), omitBackground: name === 'divider' || name.startsWith('header-') });
    fs.unlinkSync(file);
    await tab.close();
    console.log('rendered', name);
  }
  await browser.close();
})().catch((error) => { console.error(error); process.exit(1); });
