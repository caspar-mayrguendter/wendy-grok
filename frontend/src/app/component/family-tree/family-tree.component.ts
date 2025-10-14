import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { ToastrService } from 'ngx-toastr';
import { HorseService } from 'src/app/service/horse.service';
import { Horse, HorseFamilyTree } from 'src/app/dto/horse';
import { Sex } from 'src/app/dto/sex';
import { TreeNodeComponent } from '../tree-node/tree-node.component';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';

export interface TreeNode {
  horse: Horse;
  children: TreeNode[];
  expanded: boolean;
  generation: number;
}

@Component({
  selector: 'app-family-tree',
  templateUrl: './family-tree.component.html',
  styleUrls: ['./family-tree.component.scss'],
  imports: [
    TreeNodeComponent,
    FormsModule,
    CommonModule
  ]
})
export class FamilyTreeComponent implements OnInit {
  treeRoot: TreeNode | null = null;
  maxGenerations: number = 5;
  horseForDeletion: Horse | undefined;
  loading: boolean = false;
  rootHorseId: number | null = null;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private horseService: HorseService,
    private notification: ToastrService
  ) {}

  ngOnInit(): void {
    this.route.paramMap.subscribe(params => {
      const id = params.get('id');
      if (id) {
        this.rootHorseId = +id;
        this.loadFamilyTree(+id);
      }
    });
  }

  loadFamilyTree(id: number): void {
    this.loading = true;
    this.horseService.getFamilyTree(id, this.maxGenerations).subscribe({
      next: familyTree => {
        this.treeRoot = this.convertToTreeNode(familyTree, 0);
        this.loading = false;
      },
      error: error => {
        console.error('Error loading family tree', error);
        this.notification.error('Could not load horse family tree', 'Error');
        this.router.navigate(['/horses']);
        this.loading = false;
      }
    });
  }

  convertToTreeNode(familyTree: HorseFamilyTree, generation: number): TreeNode {
    // Convert the backend family tree format to our TreeNode format
    const horse: Horse = {
      id: familyTree.id,
      name: familyTree.name,
      description: '', // Not provided in family tree
      dateOfBirth: new Date(familyTree.dateOfBirth),
      sex: familyTree.sex === 'MALE' ? Sex.male : Sex.female,
      owner: undefined, // Not provided in family tree
      mother: familyTree.mother ? {
        id: familyTree.mother.id,
        name: familyTree.mother.name,
        description: '',
        dateOfBirth: new Date(familyTree.mother.dateOfBirth),
        sex: familyTree.mother.sex === 'MALE' ? Sex.male : Sex.female,
        owner: undefined,
        mother: undefined,
        father: undefined
      } : undefined,
      father: familyTree.father ? {
        id: familyTree.father.id,
        name: familyTree.father.name,
        description: '',
        dateOfBirth: new Date(familyTree.father.dateOfBirth),
        sex: familyTree.father.sex === 'MALE' ? Sex.male : Sex.female,
        owner: undefined,
        mother: undefined,
        father: undefined
      } : undefined
    };

    const node: TreeNode = {
      horse: horse,
      children: [],
      expanded: true, // Initially expanded
      generation: generation
    };

    // Add parents as children (ancestors)
    if (familyTree.mother) {
      node.children.push(this.convertToTreeNode(familyTree.mother, generation + 1));
    }

    if (familyTree.father) {
      node.children.push(this.convertToTreeNode(familyTree.father, generation + 1));
    }

    return node;
  }

  toggleNode(node: TreeNode): void {
    node.expanded = !node.expanded;
  }

  onMaxGenerationsChange(): void {
    if (this.rootHorseId) {
      this.loadFamilyTree(this.rootHorseId);
    }
  }

  getHorseGenderSymbol(horse: Horse): string {
    return horse.sex === 'FEMALE' ? '♀' : '♂';
  }

  formatBirthDate(horse: Horse): string {
    return new Date(horse.dateOfBirth).toLocaleDateString();
  }

  deleteHorse(horse: Horse): void {
    if (horse && horse.id) {
      this.horseService.delete(horse.id).subscribe({
        next: () => {
          this.notification.success(`Horse ${horse.name} successfully deleted.`);
          // Refresh the tree by reloading the family tree
          if (this.rootHorseId) {
            this.loadFamilyTree(this.rootHorseId);
          }
        },
        error: error => {
          console.error('Error deleting horse', error);
          this.notification.error('Could not delete horse', 'Error');
        }
      });
    }
  }
}
