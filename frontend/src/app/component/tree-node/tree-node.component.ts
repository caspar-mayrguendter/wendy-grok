import { Component, Input } from '@angular/core';
import { RouterLink } from '@angular/router';
import { FamilyTreeComponent, TreeNode } from '../family-tree/family-tree.component';

@Component({
  selector: 'app-tree-node',
  templateUrl: './tree-node.component.html',
  styleUrls: ['./tree-node.component.scss'],
  imports: [
    RouterLink
  ]
})
export class TreeNodeComponent {
  @Input() node!: TreeNode;
  @Input() component!: FamilyTreeComponent;

  toggleNode(): void {
    this.component.toggleNode(this.node);
  }

  getHorseGenderSymbol(): string {
    return this.component.getHorseGenderSymbol(this.node.horse);
  }

  formatBirthDate(): string {
    return this.component.formatBirthDate(this.node.horse);
  }

  deleteHorse(): void {
    this.component.deleteHorse(this.node.horse);
  }
}
