package com.arifa.assistant.core.websiteforge

object Templates {

    val RESTAURANT_TEMPLATE = """
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Dawat Royale | authentic Bengali Cuisine</title>
  <script src="https://cdnjs.cloudflare.com/ajax/libs/three.js/r128/three.min.js"></script>
  <style>
    :root {
      --bg: #0d0f17;
      --card: #151928;
      --gold: #ffb300;
      --lotus: #ff4081;
      --text: #f8f9fa;
      --muted: #a0aec0;
    }
    * { box-sizing: border-box; margin: 0; padding: 0; }
    body {
      background: var(--bg);
      color: var(--text);
      font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
      overflow-x: hidden;
    }
    nav {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 1.2rem 2rem;
      background: rgba(21, 25, 40, 0.7);
      backdrop-filter: blur(12px);
      position: sticky;
      top: 0;
      z-index: 100;
      border-bottom: 1px solid rgba(255, 179, 0, 0.2);
    }
    .logo { font-size: 1.5rem; font-weight: 800; color: var(--gold); }
    .hero {
      position: relative;
      height: 75vh;
      display: flex;
      align-items: center;
      justify-content: center;
      text-align: center;
      padding: 0 1.5rem;
    }
    #three-canvas {
      position: absolute;
      top: 0; left: 0; width: 100%; height: 100%;
      z-index: 1; pointer-events: none;
    }
    .hero-content {
      position: relative;
      z-index: 2;
      max-width: 700px;
      background: rgba(13, 15, 23, 0.6);
      padding: 2.5rem;
      border-radius: 24px;
      border: 1px solid rgba(255, 255, 255, 0.1);
      backdrop-filter: blur(10px);
    }
    .hero h1 { font-size: 2.8rem; margin-bottom: 1rem; color: var(--gold); }
    .hero p { font-size: 1.2rem; color: var(--muted); margin-bottom: 2rem; }
    .btn {
      background: linear-gradient(135deg, var(--gold), #ff8f00);
      color: #000;
      padding: 0.8rem 2rem;
      border-radius: 50px;
      font-weight: 700;
      border: none;
      cursor: pointer;
      font-size: 1rem;
      text-decoration: none;
    }
    .section { padding: 4rem 1.5rem; max-width: 1100px; margin: 0 auto; }
    .section-title { text-align: center; font-size: 2.2rem; margin-bottom: 2.5rem; color: var(--gold); }
    .grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(280px, 1fr)); gap: 1.8rem; }
    .card {
      background: var(--card);
      border-radius: 18px;
      padding: 1.8rem;
      border: 1px solid rgba(255, 255, 255, 0.08);
      transition: transform 0.3s ease, border-color 0.3s ease;
    }
    .card:hover { transform: translateY(-5px); border-color: var(--gold); }
    .card h3 { color: var(--gold); margin-bottom: 0.5rem; }
    .card p { color: var(--muted); font-size: 0.95rem; line-height: 1.5; }
    .price { display: inline-block; margin-top: 1rem; font-weight: bold; color: var(--lotus); font-size: 1.2rem; }
    footer { text-align: center; padding: 2.5rem; background: #080a10; color: var(--muted); }
  </style>
</head>
<body>
  <nav>
    <div class="logo">দাওয়াত রয়্যাল</div>
    <a href="#menu" class="btn">View Menu</a>
  </nav>

  <header class="hero">
    <canvas id="three-canvas"></canvas>
    <div class="hero-content">
      <h1>স্বাদে ঐতিহ্যে অমর বাংলা</h1>
      <p>Indulge in authentic Kacchi Biryani, Morog Polao, Ilish Paturi and heritage desserts crafted with timeless royal recipes.</p>
      <a href="#menu" class="btn">Order Now</a>
    </div>
  </header>

  <section id="menu" class="section">
    <h2 class="section-title">Specialties of the House</h2>
    <div class="grid">
      <div class="card">
        <h3>Old Dhaka Shahi Kacchi</h3>
        <p>Aromatic chinigura rice slow-cooked with tender spiced mutton, fried potatoes and saffron essence.</p>
        <span class="price">৳ ৪৫০</span>
      </div>
      <div class="card">
        <h3>Ilish Shorshe Paturi</h3>
        <p>Fresh Padma River Hilsa wrapped in charred banana leaves with stone-ground mustard paste.</p>
        <span class="price">৳ ৫২০</span>
      </div>
      <div class="card">
        <h3>Zafrani Firni</h3>
        <p>Silky royal rice pudding infused with cardamoms, pure saffron and garnished with pistachios.</p>
        <span class="price">৳ ১৫০</span>
      </div>
    </div>
  </section>

  <footer>
    <p>© 2026 Dawat Royale. Crafted with ARIFA AI Website Forge.</p>
  </footer>

  <script>
    // Three.js 3D Scene
    const canvas = document.getElementById('three-canvas');
    const scene = new THREE.Scene();
    const camera = new THREE.PerspectiveCamera(60, window.innerWidth / window.innerHeight, 0.1, 1000);
    const renderer = new THREE.WebGLRenderer({ canvas: canvas, alpha: true, antialias: true });
    renderer.setSize(window.innerWidth, window.innerHeight);

    // 3D Golden Torus Geometry
    const geometry = new THREE.TorusKnotGeometry(1.8, 0.5, 100, 16);
    const material = new THREE.MeshStandardMaterial({
      color: 0xffb300,
      metalness: 0.8,
      roughness: 0.2,
      wireframe: true
    });
    const torus = new THREE.Mesh(geometry, material);
    scene.add(torus);

    const ambientLight = new THREE.AmbientLight(0xffffff, 0.8);
    scene.add(ambientLight);
    const pointLight = new THREE.PointLight(0xff4081, 2, 50);
    pointLight.position.set(5, 5, 5);
    scene.add(pointLight);

    camera.position.z = 6;

    function animate() {
      requestAnimationFrame(animate);
      torus.rotation.x += 0.005;
      torus.rotation.y += 0.008;
      renderer.render(scene, camera);
    }
    animate();

    window.addEventListener('resize', () => {
      camera.aspect = window.innerWidth / window.innerHeight;
      camera.updateProjectionMatrix();
      renderer.setSize(window.innerWidth, window.innerHeight);
    });
  </script>
</body>
</html>
""".trim()

    val PORTFOLIO_TEMPLATE = """
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>CyberCore | AI Engineer & Architect</title>
  <script src="https://cdnjs.cloudflare.com/ajax/libs/three.js/r128/three.min.js"></script>
  <style>
    :root {
      --bg: #070913;
      --surface: #101426;
      --cyan: #00e5ff;
      --magenta: #ff4081;
      --text: #f0f4f8;
      --muted: #8892b0;
    }
    * { box-sizing: border-box; margin: 0; padding: 0; }
    body { background: var(--bg); color: var(--text); font-family: monospace, sans-serif; overflow-x: hidden; }
    nav { display: flex; justify-content: space-between; padding: 1.5rem 2rem; border-bottom: 1px solid rgba(0,229,255,0.2); backdrop-filter: blur(8px); }
    .logo { color: var(--cyan); font-size: 1.3rem; font-weight: bold; }
    .hero { position: relative; height: 80vh; display: flex; align-items: center; justify-content: center; text-align: center; }
    #canvas-bg { position: absolute; top: 0; left: 0; width: 100%; height: 100%; z-index: 1; pointer-events: none; }
    .hero-box { position: relative; z-index: 2; padding: 2rem; background: rgba(16,20,38,0.7); border-radius: 16px; border: 1px solid var(--cyan); box-shadow: 0 0 30px rgba(0,229,255,0.2); }
    h1 { font-size: 2.5rem; color: var(--cyan); margin-bottom: 0.8rem; }
    p { color: var(--muted); margin-bottom: 1.5rem; }
    .btn { background: transparent; color: var(--cyan); border: 1px solid var(--cyan); padding: 0.7rem 1.8rem; border-radius: 4px; text-decoration: none; display: inline-block; transition: 0.3s; }
    .btn:hover { background: var(--cyan); color: #000; box-shadow: 0 0 15px var(--cyan); }
    .projects { padding: 4rem 2rem; max-width: 1000px; margin: 0 auto; }
    .card-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(280px, 1fr)); gap: 1.5rem; }
    .project-card { background: var(--surface); border: 1px solid rgba(255,255,255,0.08); padding: 1.8rem; border-radius: 12px; }
    .project-card h3 { color: var(--magenta); margin-bottom: 0.5rem; }
  </style>
</head>
<body>
  <nav>
    <div class="logo">~/AI_ARCHITECT</div>
    <a href="#projects" class="btn">Deployments</a>
  </nav>
  <div class="hero">
    <canvas id="canvas-bg"></canvas>
    <div class="hero-box">
      <h1>System Architect</h1>
      <p>Building real-time neural interfaces, autonomous agents, and high-concurrency distributed engines.</p>
      <a href="#projects" class="btn">View Systems</a>
    </div>
  </div>
  <div id="projects" class="projects">
    <h2 style="color:var(--cyan); margin-bottom:2rem;">Core Deployments</h2>
    <div class="card-grid">
      <div class="project-card">
        <h3>Neural Voice Live Engine</h3>
        <p>Full-duplex bidirectional streaming WebSocket client with hardware echo cancellation.</p>
      </div>
      <div class="project-card">
        <h3>Autonomous Vision Agent</h3>
        <p>Real-time computer vision workflow execution across mobile interfaces.</p>
      </div>
    </div>
  </div>
  <script>
    const canvas = document.getElementById('canvas-bg');
    const scene = new THREE.Scene();
    const camera = new THREE.PerspectiveCamera(75, window.innerWidth/window.innerHeight, 0.1, 1000);
    const renderer = new THREE.WebGLRenderer({ canvas: canvas, alpha: true, antialias: true });
    renderer.setSize(window.innerWidth, window.innerHeight);

    const count = 700;
    const geometry = new THREE.BufferGeometry();
    const positions = new Float32Array(count * 3);
    for(let i=0; i<count*3; i++) { positions[i] = (Math.random() - 0.5) * 20; }
    geometry.setAttribute('position', new THREE.BufferAttribute(positions, 3));
    const material = new THREE.PointsMaterial({ size: 0.06, color: 0x00e5ff });
    const points = new THREE.Points(geometry, material);
    scene.add(points);
    camera.position.z = 5;

    function render() {
      requestAnimationFrame(render);
      points.rotation.y += 0.002;
      points.rotation.x += 0.001;
      renderer.render(scene, camera);
    }
    render();
  </script>
</body>
</html>
""".trim()
}
