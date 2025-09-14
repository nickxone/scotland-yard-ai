# Scotland Yard AI
AI agents for the **Scotland Yard** board game:
- `DetectivesAI` – plays as the detectives trying to catch MrX  
- `MrXAI` – plays as MrX, attempting to avoid capture  

The core decision-making is powered by a **game tree search** with **MiniMax** and **Alpha-Beta pruning**, optimised for efficiency and scalability.  

> [!IMPORTANT]
> This project was submitted as the final assignment for COMS10018: Object Oriented Programming and Algorithms module. The code should not be copied. 

---

## How It Works  

The AI explores possible future game states using a tree structure. Each node contains:  
- **Game State** (`Board.GameState`)  
- **Moves** leading to that state  
- **MrX’s Location** (exact for MrX AI, predicted for Detective AI)  
- **Score** evaluating how favourable the state is for MrX (higher = better)  

### Scoring  
The score function considers:  
- Distance from detectives (via **Dijkstra’s algorithm**)  
- Ticket availability and usage (including double/secret tickets)  
- Remaining moves  

Scores are precomputed on node creation and later refined through **MiniMax** propagation.  
Low-scoring branches are pruned early to save computation.  

### Optimisation  
- **On-demand node expansion** – nodes are generated during traversal, not upfront  
- **Alpha-Beta pruning** – skips entire subtrees that cannot affect the final outcome  
- **Result**: ~40,000 nodes can be evaluated within the game’s default time limits on a standard machine  
---
## Project Structure  
- `GameTreeNode` – abstract base class (shared logic: scoring, move generation)  
- `DetectiveTreeNode` / `MrXTreeNode` – role-specific implementations  
- `DetectivesAI` / `MrXAI` – main AI entry points  
- `TreeNodeFactory` – abstract factory for creating nodes  
  - `DetectivesNodeFactory` → `DetectiveTreeNode`  
  - `MrXNodeFactory` → `MrXTreeNode`  
---
## Design Patterns  
- **Abstract Factory** – construct tree nodes consistently per role  
- **Strategy Pattern** – swap graph helpers easily for scoring experiments  
---
## Testing  
- **Alpha-Beta Pruning**: compared against a MiniMax-only AI → identical moves, but with fewer computations  
- **Simulation**: MrX AI vs. Detectives AI across multiple games to validate play quality  
--- 
## Getting Started  

1. Clone the repository:  
   ```bash
   git clone https://github.com/nickxone/scotland-yard-ai.git
   cd scotland-yard-ai
   ```
2. Run the game:
    ```bash
    ./mvnw clean compile exec:java 
    ```

