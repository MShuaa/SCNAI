<template>
  <canvas id="particles-bg" ref="canvas"></canvas>
</template>

<script>
import { onMounted, onUnmounted } from 'vue'

export default {
  name: 'ParticlesBackground',
  setup() {
    let canvas, ctx, particles, animationId

    onMounted(() => {
      canvas = document.getElementById('particles-bg')
      if (!canvas) return

      ctx = canvas.getContext('2d')
      particles = []
      const particleCount = 150

      const particleColors = [
        'rgba(100, 255, 218, ',
        'rgba(0, 255, 255, ',
        'rgba(138, 43, 226, ',
        'rgba(0, 191, 255, ',
        'rgba(238, 130, 238, '
      ]

      function resizeCanvas() {
        canvas.width = window.innerWidth
        canvas.height = window.innerHeight
      }

      resizeCanvas()
      window.addEventListener('resize', resizeCanvas)

      class Particle {
        constructor() {
          this.x = Math.random() * canvas.width
          this.y = Math.random() * canvas.height
          this.size = Math.random() * 2 + 0.5
          this.speedX = Math.random() * 1 - 0.5
          this.speedY = Math.random() * 1 - 0.5
          this.colorIndex = Math.floor(Math.random() * particleColors.length)
          this.opacity = Math.random() * 0.7 + 0.3
          this.pulse = Math.random() * 0.02 + 0.01
          this.pulseValue = 0
        }

        update() {
          this.x += this.speedX
          this.y += this.speedY
          this.pulseValue += this.pulse
          if (this.pulseValue > Math.PI * 2) {
            this.pulseValue = 0
          }
          if (this.x > canvas.width) this.x = 0
          else if (this.x < 0) this.x = canvas.width
          if (this.y > canvas.height) this.y = 0
          else if (this.y < 0) this.y = canvas.height
        }

        draw() {
          const glowSize = this.size + Math.sin(this.pulseValue) * 0.5
          const currentOpacity = this.opacity + Math.sin(this.pulseValue) * 0.1
          ctx.fillStyle = particleColors[this.colorIndex] + currentOpacity + ')'
          ctx.beginPath()
          ctx.arc(this.x, this.y, glowSize, 0, Math.PI * 2)
          ctx.fill()
        }
      }

      function init() {
        for (let i = 0; i < particleCount; i++) {
          particles.push(new Particle())
        }
      }

      function connectParticles() {
        for (let i = 0; i < particles.length; i++) {
          for (let j = i + 1; j < particles.length; j++) {
            const dx = particles[i].x - particles[j].x
            const dy = particles[i].y - particles[j].y
            const distance = Math.sqrt(dx * dx + dy * dy)

            if (distance < 150) {
              const gradient = ctx.createLinearGradient(
                particles[i].x, particles[i].y,
                particles[j].x, particles[j].y
              )
              gradient.addColorStop(0, particleColors[particles[i].colorIndex] + (0.2 * (1 - distance / 150)) + ')')
              gradient.addColorStop(1, particleColors[particles[j].colorIndex] + (0.2 * (1 - distance / 150)) + ')')
              ctx.strokeStyle = gradient
              ctx.lineWidth = 0.5
              ctx.beginPath()
              ctx.moveTo(particles[i].x, particles[i].y)
              ctx.lineTo(particles[j].x, particles[j].y)
              ctx.stroke()
            }
          }
        }
      }

      function animate() {
        ctx.clearRect(0, 0, canvas.width, canvas.height)
        particles.forEach(particle => {
          particle.update()
          particle.draw()
        })
        connectParticles()
        animationId = requestAnimationFrame(animate)
      }

      init()
      animate()
    })

    onUnmounted(() => {
      if (animationId) {
        cancelAnimationFrame(animationId)
      }
    })
  }
}
</script>

<style scoped>
#particles-bg {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: -1;
}
</style>
