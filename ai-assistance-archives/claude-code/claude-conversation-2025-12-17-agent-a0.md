# Claude Conversation Log

Session ID: agent-a0d0415
Date: 2025-12-17 08:08:32

---

## 👤 User

Design an implementation plan for creating a full graphical node-edge layout visualization for a metabolism simulator using Compose Multiplatform.

## User Requirements:
- Full graphical node-edge layout (Canvas-based with spatial positioning and actual arrow lines)
- Merge organ metabolite information into each graph node
- No interactivity (no dragging, zoom/pan, or highlighting)
- Replace the current text-based graph visualization

## Current Implementation:

### Files:
1. **GraphVisualization.kt** (`composeApp/src/commonMain/kotlin/jon/sharp/metabolism/simulator/ui/GraphVisualization.kt`)
   - Text-based hierarchical display with Card components
   - `GraphVisualization()` - main composable
   - `NodeVisualization()` - displays single node in card
   - `EdgeVisualization()` - displays edge with "→" text

2. **OrganDisplay.kt** (`composeApp/src/commonMain/kotlin/jon/sharp/metabolism/simulator/ui/OrganDisplay.kt`)
   - Shows metabolite contents for individual organs
   - Displays organ name, optional image, and metabolites list
   - Smart number formatting for metabolite amounts

3. **BodyTransportGraph.kt** (`composeApp/src/jvmMain/kotlin/jon/sharp/metabolism/simulator/model/body/BodyTransportGraph.kt`)
   - Data structures: `OrganNode`, `DirectionalOrganEdge`, `QuantifiedMetabolite`
   - Uses identity-based equality to handle circular graphs
   - `getGraphStructure()` returns UI-friendly `List<GraphNode>`
   - Graph topology: Mouth → Stomach → Small Intestine → ... → Blood (circular with Cytosol)

4. **App.kt** - Main app that renders GraphVisualization and OrganDisplay components

### Framework:
- Jetpack Compose Multiplatform with Material3
- Canvas API available via `androidx.compose.foundation.Canvas`
- Declarative UI with state management

## Design Constraints:
- Must handle circular graph structures (e.g., Blood ↔ Cytosol)
- Should display metabolite information within nodes (merge OrganDisplay functionality)
- Layout algorithm must position nodes spatially (hierarchical/layered layout recommended)
- Arrows should clearly show direction and connect to appropriate nodes
- Should fit within existing Compose architecture

## Please provide:
1. **Layout Algorithm Choice**: Recommend a specific algorithm (hierarchical/Sugiyama, force-directed, or simple layered) with justification
2. **Node Rendering**: How to render nodes with organ info merged in (dimensions, styling, metabolite display)
3. **Arrow Rendering**: How to draw arrows between nodes (Bezier curves, straight lines, arrowhead style)
4. **File Structure**: Which files to create/modify
5. **Implementation Steps**: Ordered list of implementation tasks
6. **Critical Technical Decisions**: Any trade-offs or important design choices

Focus on a clean, maintainable implementation that follows Kotlin and Compose best practices.

---

## 🤖 Claude

I'll explore the codebase to understand the current implementation and design a comprehensive plan for the graphical node-edge visualization.

---

## 🤖 Claude

Let me find the App.kt file.

---

## 🤖 Claude

Now let me explore the Metabolite and related model classes to understand data structures better.

---

## 🤖 Claude

Let me check the project structure to understand dependencies and build configuration.

---

## 🤖 Claude

Now let me explore the actual jvmMain Body.kt to understand the full implementation.

---

## 🤖 Claude

Now I have a comprehensive understanding of the codebase. Let me design a complete implementation plan for the graphical node-edge visualization.

## Implementation Plan: Canvas-Based Graph Visualization for Metabolism Simulator

Based on my thorough exploration of the codebase, I've designed a comprehensive plan for creating a full graphical node-edge layout visualization that merges organ metabolite information into each node.

### 1. Layout Algorithm Choice: **Hierarchical Layered Layout (Sugiyama-inspired)**

**Justification:**
- The graph has a clear directional flow: Mouth → Stomach → Small Intestine → ... → Blood (with circular reference to Cytosol)
- Hierarchical layout naturally represents the metabolic pipeline
- Easier to implement than force-directed algorithms
- Provides predictable, readable layouts for biological processes
- Handles cycles gracefully by detecting back-edges

**Algorithm approach:**
1. **Layer assignment**: Use BFS from start node (Mouth) to assign nodes to layers
2. **Cycle detection**: Identify back-edges (e.g., Blood → Cytosol when Cytosol already visited)
3. **Position calculation**: Distribute nodes evenly within each layer
4. **Edge routing**: Draw straight lines for forward edges, curved Bezier for back-edges

**Trade-off consideration:** This is simpler than full Sugiyama (no crossing minimization) but sufficient for ~10 nodes. For larger graphs, we might need edge crossing reduction.

### 2. Node Rendering Design

**Node Structure:**
- **Dimensions**: Width 200dp, variable height (base 100dp + 20dp per metabolite)
- **Layout**: Rounded rectangle with border
- **Content**:
  - Header: Organ name (bold, 16sp)
  - Body: Metabolite list (reuse `MetabolitesList` from OrganDisplay.kt)
  - Footer: Optional edge count indicator

**Styling:**
- Border: 2dp, primary color
- Background: primaryContainer with slight transparency
- Text: Material3 typography
- Padding: 12dp internal

**Data Integration:**
- Create new data class `GraphNodeWithMetabolites` that combines `GraphNode` with actual metabolite data from `Body`
- Fetch metabolites via `body.organNameMap[organName]!!.getMetabolites()`

### 3. Arrow Rendering Design

**Arrow types:**
1. **Forward edges**: Straight lines with simple arrowheads
2. **Back edges**: Bezier curves (to visually distinguish cycles)

**Technical approach:**
- Use Compose `Canvas` with `drawPath` for arrows
- Calculate connection points on node borders (center-right to center-left for horizontal flow)
- Arrowhead: Small triangle (10dp) using `Path` with `moveTo`/`lineTo`
- Edge labels: Small text showing percentage for key metabolites (optional, can be minimal)

**Color coding:**
- Standard edges: secondary color
- Back edges: tertiary color (to highlight cycles)
- Edge width: 2dp for visibility

### 4. File Structure

**New files to create:**

1. **/Users/jasha/dev/ucd/PeanutsMetabolismSimulator/composeApp/src/commonMain/kotlin/jon/sharp/metabolism/simulator/ui/canvas/CanvasGraphVisualization.kt**
   - Main composable for Canvas-based graph
   - Handles overall layout and sizing

2. **/Users/jasha/dev/ucd/PeanutsMetabolismSimulator/composeApp/src/commonMain/kotlin/jon/sharp/metabolism/simulator/ui/canvas/GraphLayoutEngine.kt**
   - Pure Kotlin class for layout algorithm
   - Computes node positions and edge paths
   - Data classes: `NodePosition`, `EdgePath`, `LayoutResult`

3. **/Users/jasha/dev/ucd/PeanutsMetabolismSimulator/composeApp/src/commonMain/kotlin/jon/sharp/metabolism/simulator/ui/canvas/GraphRenderer.kt**
   - Canvas drawing functions
   - `drawNode()`, `drawEdge()`, `drawArrowhead()`
   - Separated for testability

4. **/Users/jasha/dev/ucd/PeanutsMetabolismSimulator/composeApp/src/commonMain/kotlin/jon/sharp/metabolism/simulator/ui/canvas/GraphDataModel.kt**
   - Data classes merging graph structure with metabolites
   - `GraphNodeWithMetabolites`, `PositionedNode`, `PositionedEdge`

**Files to modify:**

5. **/Users/jasha/dev/ucd/PeanutsMetabolismSimulator/composeApp/src/commonMain/kotlin/jon/sharp/metabolism/simulator/ui/App.kt**
   - Replace `GraphVisualization` call with `CanvasGraphVisualization`
   - Pass both `transportGraph` and `body` for metabolite data

6. **/Users/jasha/dev/ucd/PeanutsMetabolismSimulator/composeApp/src/commonMain/kotlin/jon/sharp/metabolism/simulator/ui/OrganDisplay.kt** (minor)
   - Extract `MetabolitesList` and `formatWeight` to be reusable
   - Move to shared utilities or keep as internal functions to be referenced

**Files to potentially deprecate:**
- GraphVisualization.kt (keep for now as reference, remove after validation)

### 5. Implementation Steps

**Phase 1: Data Layer (Foundation)**
1. Create `GraphDataModel.kt` with data classes
2. Implement function to merge `GraphNode` with metabolite data from `Body`
3. Write unit tests for data merging logic

**Phase 2: Layout Engine (Core Algorithm)**
4. Create `GraphLayoutEngine.kt` with basic structure
5. Implement BFS-based layer assignment
6. Implement cycle detection (identify back-edges)
7. Calculate node positions within layers
8. Compute edge paths (straight for forward, Bezier control points for back-edges)
9. Create `LayoutResult` containing all positioned elements

**Phase 3: Rendering Layer (Visualization)**
10. Create `GraphRenderer.kt` with drawing primitives
11. Implement `drawNode()` - rounded rect with text layout
12. Implement `drawEdge()` - path drawing with stroke
13. Implement `drawArrowhead()` - triangle at edge end
14. Test rendering with mock data

**Phase 4: Integration (Assembly)**
15. Create `CanvasGraphVisualization.kt` main composable
16. Integrate layout engine output with renderer
17. Add proper sizing/scrolling behavior
18. Handle recomposition efficiently (remember layout results)

**Phase 5: Polish & Integration**
19. Update `App.kt` to use new visualization
20. Test with actual simulation data
21. Adjust spacing, colors, fonts for readability
22. Verify build success
23. Remove old `GraphVisualization.kt` after validation

### 6. Critical Technical Decisions

**Decision 1: Layout Calculation Timing**
- **Choice**: Calculate layout in `remember` with dependency on graph structure
- **Rationale**: Layout is expensive, should only recalculate when graph topology changes (not on every metabolite update)
- **Implementation**: `val layoutResult = remember(graphStructure) { layoutEngine.computeLayout(graphData) }`

**Decision 2: Canvas Sizing**
- **Choice**: Fixed canvas size based on layout bounds, wrapped in horizontal/vertical scroll
- **Rationale**: Simple, predictable, no complex viewport management needed for non-interactive view
- **Implementation**: `Canvas(modifier = Modifier.size(layoutWidth.dp, layoutHeight.dp))`

**Decision 3: Metabolite Display Density**
- **Choice**: Show all metabolites in each node (consistent with current OrganDisplay)
- **Rationale**: Complete information, no interactivity means no hover/expand options
- **Trade-off**: Nodes may become tall; can limit to top N metabolites if needed
- **Mitigation**: Start with all, add truncation if UX suffers

**Decision 4: Edge Label Handling**
- **Choice**: Minimal or no edge labels (only show in back-edge cases if needed)
- **Rationale**: Avoid clutter; metabolite percentages already in graph data structure but not critical for visual understanding
- **Future**: Can add small percentage badges if user requests

**Decision 5: Coordinate System**
- **Choice**: Top-left origin (0,0), horizontal left-to-right flow, layers are vertical columns
- **Rationale**: Matches natural reading order and pipeline flow
- **Example**: Mouth (layer 0, x=100) → Stomach (layer 1, x=400) → etc.

**Decision 6: State Management**
- **Choice**: Stateless rendering (layout and data passed as parameters)
- **Rationale**: Follows Compose best practices, easier testing, predictable recomposition
- **Implementation**: All state hoisted to App.kt or parent composable

**Decision 7: Circular Reference Handling**
- **Choice**: Detect cycles during layout, render back-edges with Bezier curves curving backward
- **Example**: Blood→Cytosol edge will curve back from right layer to left layer
- **Rationale**: Visual distinction makes cycles obvious and maintains readability

### 7. Layout Algorithm Pseudocode

```kotlin
fun computeLayout(nodes: List<GraphNodeWithMetabolites>): LayoutResult {
    // Phase 1: Layer assignment via BFS
    val layerMap = mutableMapOf<String, Int>()
    val visited = mutableSetOf<String>()
    val queue = mutableListOf(startNode)
    layerMap[startNode.organName] = 0
    
    while (queue.isNotEmpty()) {
        val current = queue.removeFirst()
        visited.add(current.organName)
        val currentLayer = layerMap[current.organName]!!
        
        current.edges.forEach { edge ->
            if (edge.destinationName !in visited) {
                layerMap[edge.destinationName] = currentLayer + 1
                queue.add(findNode(edge.destinationName))
            } else {
                // Back edge detected - mark for curved rendering
            }
        }
    }
    
    // Phase 2: Position calculation
    val layers = groupNodesByLayer(layerMap)
    val nodePositions = mutableMapOf<String, Offset>()
    
    layers.forEach { (layer, nodesInLayer) ->
        val x = layer * HORIZONTAL_SPACING
        val verticalSpace = CANVAS_HEIGHT / (nodesInLayer.size + 1)
        nodesInLayer.forEachIndexed { index, node ->
            val y = verticalSpace * (index + 1)
            nodePositions[node.organName] = Offset(x, y)
        }
    }
    
    // Phase 3: Edge path calculation
    val edgePaths = calculateEdgePaths(nodes, nodePositions)
    
    return LayoutResult(nodePositions, edgePaths)
}
```

### 8. Code Architecture Diagram

```
App.kt
  └─> CanvasGraphVisualization(transportGraph, body)
       ├─> GraphDataModel (merges graph + metabolites)
       ├─> GraphLayoutEngine (computes positions)
       │    └─> LayoutResult(nodePositions, edgePaths)
       └─> Canvas { ... }
            └─> GraphRenderer
                 ├─> drawNode(position, metabolites)
                 ├─> drawEdge(path, isBackEdge)
                 └─> drawArrowhead(position, angle)
```

### 9. Testing Strategy

1. **Unit tests** for GraphLayoutEngine (layer assignment, cycle detection)
2. **Visual tests** with sample data (3-4 nodes, simple cycles)
3. **Integration test** with actual BodyTransportGraph
4. **Build verification** before committing

### 10. Performance Considerations

- Layout computation: O(V + E) using BFS - acceptable for ~10 nodes
- Rendering: Canvas draws on every recomposition - use `remember` for layout to minimize recalculation
- Metabolite updates: Won't trigger layout recalculation, only content redraw
- Expected performance: Smooth 60fps even with frequent metabolite updates

### Critical Files for Implementation

1. **/Users/jasha/dev/ucd/PeanutsMetabolismSimulator/composeApp/src/commonMain/kotlin/jon/sharp/metabolism/simulator/ui/canvas/GraphLayoutEngine.kt** - Core layout algorithm with BFS layer assignment and position calculation

2. **/Users/jasha/dev/ucd/PeanutsMetabolismSimulator/composeApp/src/commonMain/kotlin/jon/sharp/metabolism/simulator/ui/canvas/CanvasGraphVisualization.kt** - Main composable integrating layout engine and Canvas rendering

3. **/Users/jasha/dev/ucd/PeanutsMetabolismSimulator/composeApp/src/jvmMain/kotlin/jon/sharp/metabolism/simulator/model/body/BodyTransportGraph.kt** - Reference for graph structure and existing data model patterns

4. **/Users/jasha/dev/ucd/PeanutsMetabolismSimulator/composeApp/src/commonMain/kotlin/jon/sharp/metabolism/simulator/ui/OrganDisplay.kt** - Reuse MetabolitesList and formatWeight for consistent styling

5. **/Users/jasha/dev/ucd/PeanutsMetabolismSimulator/composeApp/src/commonMain/kotlin/jon/sharp/metabolism/simulator/ui/App.kt** - Integration point to replace text-based GraphVisualization

---

