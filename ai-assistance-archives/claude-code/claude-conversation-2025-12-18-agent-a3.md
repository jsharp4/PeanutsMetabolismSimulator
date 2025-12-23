# Claude Conversation Log

Session ID: agent-a3690ef
Date: 2025-12-18 08:43:29

---

## 👤 User

I need to add labels to graph edges showing actual metabolite quantities transported during each simulation step.

## User Requirement
Display actual quantities per step (e.g., "Glucose: 2.3 mmol") on each arrow/edge in the canvas graph visualization.

## Current Architecture (from exploration)

**Transport Flow:**
1. `Body.metabolizeTimeStep()` orchestrates the transport
2. `populateQueueFromEdges()` calculates actual quantities: `actualAmount = (percentageOfOutput / 100) × currentOrganAmount`
3. Each `DirectionalOrganEdge` has `metabolitesToSend: Set<QuantifiedMetabolite>` (type + percentageOfOutput)
4. Currently there's a `visitedEdgeMap: Map<DirectionalOrganEdge, Int>` that only tracks visit counts

**Visualization Pipeline:**
1. `CanvasGraphVisualization` composable uses `createGraphWithMetabolites()` to merge graph structure with metabolites
2. `GraphLayoutEngine.computeLayout()` creates `EdgePath` objects with waypoints for rendering
3. `drawGraphEdge()` in GraphRenderer.kt renders the edge lines and arrowheads
4. Current `EdgePath` data class:
```kotlin
data class EdgePath(
    val fromNode: String,
    val toNode: String,
    val isBackEdge: Boolean,
    val waypoints: List<Offset>,
    val color: Color
)
```

**Key files:**
- `/Users/jasha/dev/ucd/PeanutsMetabolismSimulator/composeApp/src/jvmMain/kotlin/jon/sharp/metabolism/simulator/model/body/Body.kt` (transport logic)
- `/Users/jasha/dev/ucd/PeanutsMetabolismSimulator/composeApp/src/commonMain/kotlin/jon/sharp/metabolism/simulator/ui/canvas/GraphDataModel.kt` (data structures)
- `/Users/jasha/dev/ucd/PeanutsMetabolismSimulator/composeApp/src/commonMain/kotlin/jon/sharp/metabolism/simulator/ui/canvas/GraphLayoutEngine.kt` (layout)
- `/Users/jasha/dev/ucd/PeanutsMetabolismSimulator/composeApp/src/commonMain/kotlin/jon/sharp/metabolism/simulator/ui/canvas/GraphRenderer.kt` (rendering)
- `/Users/jasha/dev/ucd/PeanutsMetabolismSimulator/composeApp/src/commonMain/kotlin/jon/sharp/metabolism/simulator/ui/canvas/CanvasGraphVisualization.kt` (main composable)

## Design Requirements

1. **Capture transported quantities:** Modify Body.kt to track actual quantities transported on each edge during each simulation step
2. **Data flow:** Make this data accessible to the visualization layer (need to bridge JVM model to common UI)
3. **Data structure:** Extend EdgePath or create new structure to hold metabolite quantities
4. **Rendering:** Add label drawing function that positions text along edge paths
5. **Performance:** Ensure tracking doesn't impact simulation performance
6. **Updates:** Labels should update on each simulation step (respects updateCount mechanism)

## Constraints
- Follow existing patterns (remember blocks, updateCount for recomposition)
- Use formatWeight() utility for consistent formatting
- Labels should be readable (background box, good positioning)
- Don't clutter the display if many metabolites (consider summarization or stacking)
- Maintain DRY, KISS, SRP principles

Please design a detailed implementation plan covering:
1. How to capture and store transported quantities in Body.kt
2. How to make this data accessible to visualization (Body → BodyTransportGraph → GraphNode → EdgePath)
3. Data structure changes needed (EdgePath, new classes, etc.)
4. Label positioning strategy on orthogonal paths
5. Rendering approach (background boxes, text styling, positioning)
6. Any architectural trade-offs or alternatives to consider

Consider multiple approaches and recommend the best one.

---

## 🤖 Claude

I'll explore the codebase to understand the current architecture and design a comprehensive implementation plan for adding metabolite quantity labels to graph edges.

---

## 🤖 Claude

Now let me look at how the simulation engine coordinates with the visualization to understand the update mechanism.

---

## 🤖 Claude

Perfect! I now have a comprehensive understanding of the codebase architecture. Let me design a detailed implementation plan.

## Architecture Analysis Summary

**Key Findings:**

1. **Transport calculation happens in Body.kt** - The `populateQueueFromEdges()` method calculates actual amounts using: `actualAmount = (percentageOfOutput / 100) × currentOrganAmount` (lines 73-74, 79-80)

2. **Current tracking mechanism** - `visitedEdgeMap` only tracks visit counts, not quantities

3. **Data flow layers:**
   - JVM layer: `Body`, `BodyTransportGraph`, `DirectionalOrganEdge`, `QuantifiedMetabolite`
   - Common layer: `GraphNode`, `GraphEdge`, `MetaboliteTransfer`, `EdgePath`
   - The bridge happens in `BodyTransportGraph.getGraphStructure()` and `createGraphWithMetabolites()`

4. **Update mechanism** - `updateCount` in App.kt triggers recomposition via `key(updateCount)` wrapper

5. **Existing utilities** - `formatWeight()` already exists for consistent formatting

## Design Considerations

### Approach 1: Store quantities in Body (JVM layer)
**Pros:**
- Captures data at the source where calculations happen
- Accurate representation of what was actually transported
- Can track historical data if needed

**Cons:**
- Adds state to Body that's purely for visualization
- Violates separation of concerns (model shouldn't know about UI needs)
- Requires bridging JVM to common layer

### Approach 2: Calculate on-demand during rendering
**Pros:**
- No state storage needed
- Clean separation of concerns
- Simpler architecture

**Cons:**
- Would need to recalculate from percentages and current organ amounts
- May not reflect what was ACTUALLY transported in the last step
- Timing issues - organ amounts change after transport

### Approach 3: Hybrid - Capture in Body, expose through graph structure (RECOMMENDED)
**Pros:**
- Accurate data capture at calculation point
- Clean data flow through existing pipeline
- Maintains separation with proper abstraction
- Minimal architectural changes

**Cons:**
- Requires extending several data structures
- Slightly more complex than Approach 2

## Recommended Implementation Plan

### Phase 1: Data Capture in Body.kt

**Objective:** Capture actual transported quantities during the transport phase.

**Implementation:**
1. Add a new map in Body.kt to store edge transport data:
   ```kotlin
   var edgeTransportMap = mutableMapOf<DirectionalOrganEdge, MetaboliteMap>()
   ```

2. Modify `populateQueueFromEdges()` to capture the calculated quantities:
   - When calculating `actualAmount`, store it in `edgeTransportMap`
   - Key: the `DirectionalOrganEdge`
   - Value: `MetaboliteMap` containing all metabolites transported on that edge

3. Reset `edgeTransportMap` at the beginning of each `metabolizeTimeStep()` (similar to how `visitedEdgeMap` is reset)

**Rationale:** This captures the data at the exact point where it's calculated, ensuring accuracy.

### Phase 2: Bridge Data to Common Layer

**Objective:** Make transported quantities accessible to the common visualization layer.

**Implementation:**
1. Extend `MetaboliteTransfer` in common layer to include actual amount:
   ```kotlin
   data class MetaboliteTransfer(
       val type: MetaboliteType,
       val percentage: Float,
       val actualAmount: Float = 0f  // Amount transported in last step
   )
   ```

2. Modify `BodyTransportGraph.getGraphStructure()` to populate actual amounts:
   - Access `Body.edgeTransportMap` (need to pass Body reference or move map to BodyTransportGraph)
   - Map `DirectionalOrganEdge` to corresponding `GraphEdge` and populate `actualAmount`

3. Alternative: Add a method to Body to retrieve edge transport data by organ names:
   ```kotlin
   fun getEdgeTransportData(fromOrgan: String, toOrgan: String): MetaboliteMap
   ```

**Trade-off consideration:** 
- Option A: Store map in `BodyTransportGraph` instead of `Body` - cleaner separation
- Option B: Store in `Body`, expose via method - simpler but tighter coupling
- **Recommendation:** Option A - move the map to `BodyTransportGraph`

### Phase 3: Extend EdgePath for Label Data

**Objective:** Add metabolite quantity information to EdgePath for rendering.

**Implementation:**
1. Extend `EdgePath` data class:
   ```kotlin
   data class EdgePath(
       val fromNode: String,
       val toNode: String,
       val isBackEdge: Boolean,
       val waypoints: List<Offset>,
       val color: Color,
       val metabolites: List<MetaboliteTransfer> = emptyList()  // NEW
   )
   ```

2. Modify `GraphLayoutEngine.calculateEdgePaths()` to populate metabolites:
   - Extract metabolite data from `GraphEdge.metabolites` (which now has actualAmount)
   - Include in EdgePath construction

**Rationale:** This keeps all edge rendering data together in one structure.

### Phase 4: Label Positioning Strategy

**Objective:** Determine optimal positions for labels along orthogonal paths.

**Strategy:**
1. **For simple vertical edges:** Place label at midpoint
2. **For orthogonal edges with waypoints:** Place at the horizontal segment midpoint
3. **For back edges:** Place on the side route segment

**Algorithm:**
```kotlin
fun calculateLabelPosition(waypoints: List<Offset>): Offset {
    // For vertical-only paths (2 waypoints)
    if (waypoints.size == 2) {
        return Offset(
            x = waypoints[0].x,
            y = (waypoints[0].y + waypoints[1].y) / 2
        )
    }
    
    // For orthogonal paths, find the horizontal segment
    for (i in 0 until waypoints.size - 1) {
        val start = waypoints[i]
        val end = waypoints[i + 1]
        
        // Horizontal segment (y is same, x differs)
        if (kotlin.math.abs(start.y - end.y) < 1f && kotlin.math.abs(start.x - end.x) > 10f) {
            return Offset(
                x = (start.x + end.x) / 2,
                y = start.y
            )
        }
    }
    
    // Fallback: middle of path
    return waypoints[waypoints.size / 2]
}
```

### Phase 5: Label Rendering

**Objective:** Render metabolite quantity labels on edges with good readability.

**Implementation:**
1. Add new function in GraphRenderer.kt:
   ```kotlin
   fun DrawScope.drawEdgeLabel(
       position: Offset,
       metabolites: List<MetaboliteTransfer>,
       color: Color,
       textMeasurer: TextMeasurer,
       colorScheme: ColorScheme
   )
   ```

2. **Label content strategy:**
   - If 1-2 metabolites: Show all (e.g., "Glucose: 2.3 mmol\nPyruvate: 1.5 mmol")
   - If 3+ metabolites: Show count (e.g., "3 metabolites" with tooltip potential)
   - Alternative: Show only metabolites with actualAmount > threshold

3. **Visual design:**
   - Semi-transparent background box (slightly darker than edge color)
   - White or high-contrast text
   - Small padding (4-6px)
   - Font size: 10-11sp (smaller than node text)
   - Rounded corners for polish

4. **Layout considerations:**
   - Measure text first to determine background box size
   - Center text within background
   - Offset label slightly above/beside the line to avoid overlap
   - For horizontal segments: place above the line
   - For vertical segments: place to the right of the line

5. **Example implementation:**
   ```kotlin
   fun DrawScope.drawEdgeLabel(
       position: Offset,
       metabolites: List<MetaboliteTransfer>,
       color: Color,
       textMeasurer: TextMeasurer,
       colorScheme: ColorScheme
   ) {
       // Filter to only show metabolites with actual transport
       val activeMetabolites = metabolites.filter { it.actualAmount > 0.001f }
       if (activeMetabolites.isEmpty()) return
       
       // Build label text
       val labelText = if (activeMetabolites.size <= 2) {
           activeMetabolites.joinToString("\n") { 
               "${it.type}: ${formatWeight(it.actualAmount)}"
           }
       } else {
           "${activeMetabolites.size} metabolites"
       }
       
       val textStyle = TextStyle(
           fontSize = 10.sp,
           fontWeight = FontWeight.Normal,
           color = colorScheme.onSurface
       )
       
       val textResult = textMeasurer.measure(labelText, textStyle)
       
       // Background box
       val padding = 4f
       val boxWidth = textResult.size.width + (padding * 2)
       val boxHeight = textResult.size.height + (padding * 2)
       
       val boxTopLeft = Offset(
           x = position.x - (boxWidth / 2),
           y = position.y - boxHeight - 8f  // Offset above the line
       )
       
       // Draw semi-transparent background
       drawRoundRect(
           color = color.copy(alpha = 0.9f),
           topLeft = boxTopLeft,
           size = Size(boxWidth, boxHeight),
           cornerRadius = CornerRadius(4f, 4f)
       )
       
       // Draw text
       drawText(
           textLayoutResult = textResult,
           topLeft = Offset(
               x = boxTopLeft.x + padding,
               y = boxTopLeft.y + padding
           )
       )
   }
   ```

6. **Integrate into rendering pipeline:**
   - Modify `drawGraphEdge()` to accept optional metabolites parameter
   - Call `drawEdgeLabel()` after drawing the edge line and arrowhead
   - Update `CanvasGraphVisualization` to pass metabolites when rendering edges

### Phase 6: Performance Optimization

**Considerations:**
1. **Memory:** `edgeTransportMap` is reset each step, so no accumulation
2. **Calculation:** Minimal overhead - just storing already-calculated values
3. **Rendering:** Text measurement can be expensive
   - Consider caching label dimensions for identical metabolite sets
   - Limit to showing only "active" metabolites (amount > threshold)
4. **Recomposition:** `key(updateCount)` already handles proper recomposition

**Optimizations:**
- Only render labels for edges with non-zero transport
- Consider LOD (Level of Detail): hide labels if canvas is zoomed out (future enhancement)

## Alternative Approaches Considered

### Alternative A: Store in DirectionalOrganEdge
**Rejected because:** Would make the graph structure mutable and couple it to runtime state

### Alternative B: Pass quantities directly to visualization
**Rejected because:** Would bypass the clean layer separation and make testing harder

### Alternative C: Use a separate tracking service
**Rejected because:** Adds unnecessary complexity when the data naturally flows through existing structures

## Architectural Trade-offs

1. **Purity vs. Pragmatism:**
   - Storing transport data in `BodyTransportGraph` adds state to what could be a pure structure
   - However, it's the most natural place since it already bridges model and visualization
   - **Decision:** Pragmatism wins - add to BodyTransportGraph with clear documentation

2. **Accuracy vs. Performance:**
   - Could sample/throttle updates for performance
   - **Decision:** Start with accuracy, optimize only if needed (profiling first)

3. **Detail vs. Clutter:**
   - Showing all metabolites could clutter the display
   - **Decision:** Smart filtering (threshold) and adaptive display (count vs. details)

## Implementation Sequence

1. **Step 1:** Add `edgeTransportMap` to BodyTransportGraph and capture in Body.kt
2. **Step 2:** Extend `MetaboliteTransfer` with `actualAmount` field
3. **Step 3:** Update `BodyTransportGraph.getGraphStructure()` to populate actual amounts
4. **Step 4:** Extend `EdgePath` with metabolites field
5. **Step 5:** Update `GraphLayoutEngine.calculateEdgePaths()` to include metabolite data
6. **Step 6:** Implement `calculateLabelPosition()` helper
7. **Step 7:** Implement `drawEdgeLabel()` rendering function
8. **Step 8:** Update `drawGraphEdge()` to render labels
9. **Step 9:** Test with various metabolite counts and verify formatting
10. **Step 10:** Optimize based on performance profiling if needed

## Testing Strategy

1. **Unit tests:** 
   - Label position calculation with different waypoint configurations
   - Text formatting for edge cases (very small, very large values)

2. **Integration tests:**
   - Verify quantities match calculated amounts
   - Check that updateCount triggers proper recomposition

3. **Visual tests:**
   - Multiple metabolites per edge
   - Back edges vs. forward edges
   - Overlapping edges (parallel routing)

## Data Structure Changes Summary

### JVM Layer (Body.kt, BodyTransportGraph.kt):
```kotlin
// In BodyTransportGraph.kt
class BodyTransportGraph {
    internal var edgeTransportMap = mutableMapOf<DirectionalOrganEdge, MetaboliteMap>()
    
    fun getEdgeTransportData(edge: DirectionalOrganEdge): MetaboliteMap {
        return edgeTransportMap[edge] ?: MetaboliteMap()
    }
}

// In Body.kt - modify populateQueueFromEdges
private fun populateQueueFromEdges(...) {
    edges.forEach { edge ->
        val transportedMetabolites = MetaboliteMap()
        edge.metabolitesToSend.forEach { metabolite ->
            val actualAmount = metabolite.percentageOfOutput / 100 * 
                              getMetaboliteFromOrganOrZero(currentNode.organ, metabolite.type)
            transportedMetabolites.putOrAdd(Metabolite(metabolite.type, actualAmount))
        }
        transportGraph.edgeTransportMap[edge] = transportedMetabolites
        // ... rest of existing logic
    }
}

// Reset in metabolizeTimeStep
actual suspend fun metabolizeTimeStep(...) {
    transportGraph.edgeTransportMap = mutableMapOf()
    // ... existing code
}
```

### Common Layer (BodyTransportGraph.kt - common):
```kotlin
data class MetaboliteTransfer(
    val type: MetaboliteType,
    val percentage: Float,
    val actualAmount: Float = 0f
)
```

### Common Layer (GraphDataModel.kt):
```kotlin
data class EdgePath(
    val fromNode: String,
    val toNode: String,
    val isBackEdge: Boolean,
    val waypoints: List<Offset>,
    val color: Color,
    val metabolites: List<MetaboliteTransfer> = emptyList()
)
```

## Handling Edge Cases

1. **Zero transport:** Don't show label (filter by threshold)
2. **Many metabolites:** Show count instead of list
3. **Overlapping labels:** Parallel edge offset should help; consider staggering vertically if needed
4. **Very small values:** formatWeight() already handles scientific notation
5. **Circular references:** Already handled by existing graph structure

### Critical Files for Implementation

- /Users/jasha/dev/ucd/PeanutsMetabolismSimulator/composeApp/src/jvmMain/kotlin/jon/sharp/metabolism/simulator/model/body/Body.kt - Core transport logic where actual quantities are calculated; need to capture data here
- /Users/jasha/dev/ucd/PeanutsMetabolismSimulator/composeApp/src/jvmMain/kotlin/jon/sharp/metabolism/simulator/model/body/BodyTransportGraph.kt - Bridge between JVM model and common UI; need to store edgeTransportMap and modify getGraphStructure()
- /Users/jasha/dev/ucd/PeanutsMetabolismSimulator/composeApp/src/commonMain/kotlin/jon/sharp/metabolism/simulator/ui/canvas/GraphDataModel.kt - Data structures; need to extend MetaboliteTransfer and EdgePath
- /Users/jasha/dev/ucd/PeanutsMetabolismSimulator/composeApp/src/commonMain/kotlin/jon/sharp/metabolism/simulator/ui/canvas/GraphRenderer.kt - Rendering logic; need to add drawEdgeLabel() and update drawGraphEdge()
- /Users/jasha/dev/ucd/PeanutsMetabolismSimulator/composeApp/src/commonMain/kotlin/jon/sharp/metabolism/simulator/ui/canvas/GraphLayoutEngine.kt - Layout calculations; need to update calculateEdgePaths() and add label positioning logic

---

