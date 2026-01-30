import numpy as np
import matplotlib.pyplot as plt
from scipy.stats import zipf

def generate_bodies(n_bodies, position_scale=1000, velocity_scale=30, 
                   mass_scale=10000, density=0.1):
    """
    Generate n bodies with:
    - Normal distribution for positions and velocities
    - Zipf distribution for masses
    
    Args:
        n_bodies: Number of bodies to generate
        position_scale: Standard deviation for position distribution
        velocity_scale: Standard deviation for velocity distribution
        mass_scale: Base scale for masses
        zipf_param: Parameter for Zipf distribution (higher = more skewed)
        density: Constant density for all bodies
    
    Returns:
        A string formatted for the Java properties file
    """
    
    # Generate positions (normal distribution)
    positions = np.random.normal(0, position_scale, (n_bodies, 3))
    
    # Generate velocities (normal distribution)
    velocities = np.random.normal(0, velocity_scale, (n_bodies, 3))
    
    # Generate masses (Zipf distribution)
    # Zipf distribution produces integers, so we scale them
    masses = np.random.uniform(mass_scale/10, mass_scale*2, n_bodies)
    np.random.shuffle(masses)  # Shuffle to avoid correlation with position
    
    # Create the properties file content
    output = []
    output.append("# Physics system setup")
    output.append("")
    output.append("# System name:")
    output.append(f"name=Random System ({n_bodies} bodies)")
    output.append("# Gravitational constant (unrealistic units)")
    output.append("gravity=10.0")
    output.append("# Simulation time step (seconds)")
    output.append("timeStep=0.004")
    output.append("# Keys for defined bodies")
    
    body_keys = [f"body{i}" for i in range(n_bodies)]
    output.append(f"bodies={' '.join(body_keys)}")
    output.append("")
    
    # Add each body
    for i in range(n_bodies):
        output.append(f"# Body {i}:")
        output.append(f"{body_keys[i]}.name=Body {i}")
        output.append(f"{body_keys[i]}.mass={masses[i]:.1f}")
        output.append(f"{body_keys[i]}.density={density}")
        output.append(f"{body_keys[i]}.position={positions[i][0]:.1f},{positions[i][1]:.1f},{positions[i][2]:.1f}")
        output.append(f"{body_keys[i]}.velocity={velocities[i][0]:.1f},{velocities[i][1]:.1f},{velocities[i][2]:.1f}")
        output.append(f"{body_keys[i]}.color=200,200,200")  # Light gray
        output.append("")
    
    return "\n".join(output)

def visualize_distributions(n_bodies, position_scale=1000, velocity_scale=30, 
                          mass_scale=10000, zipf_param=1.5):
    """Visualize the distributions to verify they look reasonable"""
    
    # Generate the data
    positions = np.random.normal(0, position_scale, (n_bodies, 3))
    velocities = np.random.normal(0, velocity_scale, (n_bodies, 3))
    ranks = np.arange(1, n_bodies + 1)
    masses = zipf.pmf(ranks, zipf_param) * mass_scale * n_bodies
    np.random.shuffle(masses)
    
    # Create visualization
    fig, axes = plt.subplots(2, 2, figsize=(12, 10))
    
    # Position distribution
    axes[0, 0].hist(positions.flatten(), bins=30, alpha=0.7)
    axes[0, 0].set_title('Position Distribution (all axes)')
    axes[0, 0].set_xlabel('Position')
    
    # Velocity distribution
    axes[0, 1].hist(velocities.flatten(), bins=30, alpha=0.7)
    axes[0, 1].set_title('Velocity Distribution (all axes)')
    axes[0, 1].set_xlabel('Velocity')
    
    # Mass distribution
    axes[1, 0].hist(masses, bins=30, alpha=0.7)
    axes[1, 0].set_title('Mass Distribution (Zipf)')
    axes[1, 0].set_xlabel('Mass')
    axes[1, 0].set_yscale('log')
    
    # 3D scatter plot of positions
    ax3d = fig.add_subplot(2, 2, 4, projection='3d')
    ax3d.scatter(positions[:, 0], positions[:, 1], positions[:, 2], 
                 s=masses/masses.max()*50, alpha=0.6)
    ax3d.set_title('3D Position Distribution')
    ax3d.set_xlabel('X')
    ax3d.set_ylabel('Y')
    ax3d.set_zlabel('Z')
    
    plt.tight_layout()
    plt.show()

def main():
    # Parameters
    n_bodies = 100
    
    # Generate and save the system
    system_config = generate_bodies(n_bodies)
    
    # Save to file
    with open('out/random_system.properties', 'w') as f:
        f.write(system_config)
    
    # Create visualization
    visualize_distributions(n_bodies)
    
    print(f"Generated {n_bodies} bodies in 'random_system.properties'")
    print("Visualization saved to 'body_distributions.png'")
    
    # Show some statistics
    positions = np.random.normal(0, 1000, (n_bodies, 3))
    velocities = np.random.normal(0, 30, (n_bodies, 3))
    ranks = np.arange(1, n_bodies + 1)
    masses = zipf.pmf(ranks, 1.5) * 10000 * n_bodies
    
    print(f"\nStatistics:")
    print(f"Position range: [{positions.min():.1f}, {positions.max():.1f}]")
    print(f"Velocity range: [{velocities.min():.1f}, {velocities.max():.1f}]")
    print(f"Mass range: [{masses.min():.1f}, {masses.max():.1f}]")
    print(f"Largest mass / smallest mass: {masses.max()/masses.min():.1f}")

if __name__ == "__main__":
    main()
